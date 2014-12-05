/*
 * CloseSweep.cpp
 *
 *  Created on: Sep 25, 2014
 *      Author: nek3d
 */

#include "CloseSweep.h"
#include "ContextClosest.h"

CloseSweep::CloseSweep(ContextClosest *context)
:	NewChromSweep(context),
 	_context(context)
 	{

	_minUpstreamRecs.resize(_numDBs, NULL);
	_minDownstreamRecs.resize(_numDBs, NULL);
	_overlapRecs.resize(_numDBs, NULL);
	_minUpstreamDist.resize(_numDBs, INT_MAX);
	_minDownstreamDist.resize(_numDBs,INT_MAX);
	_maxPrevLeftClosestEndPos.resize(_numDBs, 0);

	for (int i=0; i < _numDBs; i++) {
		_minUpstreamRecs[i] = new distRecVecType();
		_minDownstreamRecs[i] = new distRecVecType();
		_overlapRecs[i] = new vector<const Record *>();
	}

}

CloseSweep::~CloseSweep(void) {
	for (int i=0; i < _numDBs; i++) {
		delete _minUpstreamRecs[i];
		delete _minDownstreamRecs[i];
		delete _overlapRecs[i];
	}
}

bool CloseSweep::init() {

    bool retVal =  NewChromSweep::init();
    _runToQueryEnd = true;
    return retVal;
 }

void CloseSweep::masterScan(RecordKeyVector &retList) {
	if (_context->reportDistance()) {
		_finalDistances.clear();
	}

	//initialize distances
	for (int i=0; i < _numDBs; i++) {
		_minUpstreamDist[i] = INT_MAX;
		_minDownstreamDist[i] = INT_MAX;
	}

	for (int i=0; i < _numDBs; i++) {

		//first clear out everything from the previous scan
		_minUpstreamRecs[i]->clear();
		_minDownstreamRecs[i]->clear();
		_overlapRecs[i]->clear();

		if (dbFinished(i) || chromChange(i, retList, true)) {
			continue;
		} else {

			// scan the database cache for hits
			scanCache(i, retList);

			// skip if we hit the end of the DB
			// advance the db until we are ahead of the query. update hits and cache as necessary
			bool stopScanning = false;
			while (_currDbRecs[i] != NULL &&
					_currQueryRec->sameChrom(_currDbRecs[i]) &&
					!stopScanning) {
				if (considerRecord(_currDbRecs[i], i, stopScanning) == DELETE) {
					_dbFRMs[i]->deleteRecord(_currDbRecs[i]);
					_currDbRecs[i] = NULL;
				} else {
					_caches[i].push_back(_currDbRecs[i]);
					_currDbRecs[i] = NULL;
				}
				nextRecord(false, i);
			}
		}
		finalizeSelections(i, retList);
	}
	checkMultiDbs(retList);
}

void CloseSweep::scanCache(int dbIdx, RecordKeyVector &retList) {
	recListIterType cacheIter = _caches[dbIdx].begin();
    while (cacheIter != _caches[dbIdx].end())
    {
    	const Record *cacheRec = cacheIter->value();
    	bool stopScanning = false;
    	if (considerRecord(cacheRec, dbIdx, stopScanning) == DELETE) {
            cacheIter = _caches[dbIdx].deleteCurrent();
    		_dbFRMs[dbIdx]->deleteRecord(cacheRec);
    	} else {
            cacheIter = _caches[dbIdx].next();
    	}
    	if (stopScanning) break;
    }
}


CloseSweep::rateOvlpType CloseSweep::considerRecord(const Record *cacheRec, int dbIdx, bool &stopScanning) {

	if (_context->diffNames() && cacheRec->getName() == _currQueryRec->getName()) {

		// We can ignore this, but we need to know whether to stop scanning.
		// do so IF:
		// 1 ) We are not ignoring downstream hits, AND
		// 2 ) The hit is after the query record, AND
		// 3 ) Some downstream hits have been found.
		if (!_context->ignoreDownstream() && cacheRec->after(_currQueryRec) && !_minDownstreamRecs[dbIdx]->empty()) {
			stopScanning = true;
		}

		// Secondly, we also want to know whether to delete this hit from the cache.
		//
		// TBD: Not sure how to determine this accurately. Leave it for now and hope the
		// performance doesn't suffer too badly.


		return IGNORE;
	}

	// If strand is specified, and either record has an unknown strand, ignore
	if ((_context->getSameStrand() || _context->getDiffStrand()) && ((_currQueryRec->getStrandVal() == Record::UNKNOWN) || cacheRec->getStrandVal() == Record::UNKNOWN)) {
		return IGNORE;
	}
	// If want same strand, and aren't sure they're the same, ignore
	if (_context->getSameStrand() &&  (_currQueryRec->getStrandVal() != cacheRec->getStrandVal())) {
		return IGNORE;
	}
	// If we want diff strand, and aren't sure they're different, ignore.
	if (_context->getDiffStrand() && (_currQueryRec->getStrandVal() == cacheRec->getStrandVal())) {
		return IGNORE;
	}

	// Now determine whether the hit and query intersect, and if so, what to do about it.
	int currDist = 0;

	if (intersects(_currQueryRec, cacheRec)) {
		// HIT INTERSECTS QUERY
		if (!_context->ignoreOverlaps()) {
			_overlapRecs[dbIdx]->push_back(cacheRec);
			_maxPrevLeftClosestEndPos[dbIdx] = cacheRec->getEndPos();
		}
		return IGNORE;
	} else if (cacheRec->after(_currQueryRec)) {
		// HIT IS TO THE RIGHT OF THE QUERY.

		//establish distance. If it's greater than the max downstream distance,
		//we can stop scanning.
		 currDist = (cacheRec->getStartPos() - _currQueryRec->getEndPos()) + 1;
		 if (_context->signDistance()) {
			 if ((_context->getStrandedDistMode() == ContextClosest::A_DIST && _currQueryRec->getStrandVal() == Record::REVERSE) ||
				 (_context->getStrandedDistMode() == ContextClosest::B_DIST && cacheRec->getStrandVal() == Record::REVERSE))
			 {
				 // hit is "upstream" of A
				 if (_context->ignoreUpstream()) {
					 return IGNORE;
				 }
				 else {
					 if (currDist <= abs(_minUpstreamDist[dbIdx])) {
						 if (currDist < abs(_minUpstreamDist[dbIdx])) {
							 _minUpstreamDist[dbIdx] = currDist * -1;
							 _minUpstreamRecs[dbIdx]->clear();
						 }
						 _minUpstreamRecs[dbIdx]->push_back(cacheRec);
						 return IGNORE;
					 } else if (currDist == abs(_minUpstreamDist[dbIdx])) {
						 _minUpstreamRecs[dbIdx]->push_back(cacheRec);
						 return IGNORE;
					 } else {
						 return DELETE;
					 }
				 }
			 }
		 }
		 // HIT IS DOWNSTREAM.
		 // MUST FIRST DETERMINE WHETHER TO STOP SCANNING.
		 if (currDist > abs(_minDownstreamDist[dbIdx])) {
			 stopScanning = true;
			 return IGNORE;
		 }
		 if (_context->ignoreDownstream()) {
			 return IGNORE;
		 }
		 //Still here? Valid hit.
		 if (currDist <= abs(_minDownstreamDist[dbIdx])) {
			 if (currDist < abs(_minDownstreamDist[dbIdx])) {
				 _minDownstreamDist[dbIdx] = currDist;
				 _minDownstreamRecs[dbIdx]->clear();
			 }
			 _minDownstreamRecs[dbIdx]->push_back(cacheRec);
			 return IGNORE;
		 }
	 } else if (_currQueryRec->after(cacheRec)){
		 // HIT IS TO THE LEFT OF THE QUERY.

		// First see if we can purge this record from the cache. If it's further left than the last record that was left, delete it.
		if (cacheRec->getEndPos() < _maxPrevLeftClosestEndPos[dbIdx]) return DELETE;

		 currDist = (_currQueryRec->getStartPos() - cacheRec->getEndPos()) + 1;
		 if (_context->signDistance()) {
			 if ((_context->getStrandedDistMode() == ContextClosest::REF_DIST) ||
				 (_context->getStrandedDistMode() == ContextClosest::A_DIST && _currQueryRec->getStrandVal() != Record::REVERSE) ||
				 (_context->getStrandedDistMode() == ContextClosest::B_DIST && cacheRec->getStrandVal() != Record::REVERSE))
			 {
				 // HIT IS DOWNSTREAM.
				 // MUST FIRST DETERMINE WHETHER TO STOP SCANNING.
				 if (currDist > abs(_minDownstreamDist[dbIdx])) {
					 return DELETE;
				 }
				 if (_context->ignoreDownstream()) {
					 return IGNORE;
				 }
				 //Still here? Valid hit.
				 if (currDist <= abs(_minDownstreamDist[dbIdx])) {
					 if (currDist < abs(_minDownstreamDist[dbIdx])) {
						 _minDownstreamDist[dbIdx] = currDist * -1;
						 _minDownstreamRecs[dbIdx]->clear();
					 }
					 _minDownstreamRecs[dbIdx]->push_back(cacheRec);
					 _maxPrevLeftClosestEndPos[dbIdx] = cacheRec->getEndPos();
					 return IGNORE;
				 }
			 }
		 }
		 // hit is "upstream" of A
		 if (_context->ignoreUpstream()) {
			 return IGNORE;
		 }
		 if (currDist <= abs(_minUpstreamDist[dbIdx])) {
			 if (currDist < abs(_minUpstreamDist[dbIdx])) {
				 _minUpstreamDist[dbIdx] = currDist;
				 _minUpstreamRecs[dbIdx]->clear();
			 }
			 _minUpstreamRecs[dbIdx]->push_back(cacheRec);
			 _maxPrevLeftClosestEndPos[dbIdx] = cacheRec->getEndPos();

			 return IGNORE;
		 } else if (currDist == abs(_minUpstreamDist[dbIdx])) {
			 _minUpstreamRecs[dbIdx]->push_back(cacheRec);
			 _maxPrevLeftClosestEndPos[dbIdx] = cacheRec->getEndPos();
			 return IGNORE;
		 } else {
			 return DELETE;
		 }
	 }
	return IGNORE;
}


void CloseSweep::finalizeSelections(int dbIdx, RecordKeyVector &retList) {

	//check all actual overlaps, as well as upstream and downstream hits.
	// if all of these are empty, return.

	const vector<const Record *>  & overlapRecs = (*(_overlapRecs[dbIdx]));
	const vector<const Record *>  & upRecs = (*(_minUpstreamRecs[dbIdx]));
	const vector<const Record *>  & downRecs = (*(_minDownstreamRecs[dbIdx]));

	if (overlapRecs.empty() && upRecs.empty() && downRecs.empty()) {
		return;
	}


	// If there are actual overlaps, only report those, then stop.
	ContextClosest::tieModeType tieMode = _context->getTieMode();
	if (!overlapRecs.empty()) {
		if (tieMode == ContextClosest::FIRST_TIE) {
			retList.push_back(overlapRecs[0]);
			_finalDistances.push_back(0);
		} else if (tieMode == ContextClosest::LAST_TIE) {
			retList.push_back(overlapRecs[overlapRecs.size()-1]);
			_finalDistances.push_back(0);
		} else {

			for (int i=0; i < (int)overlapRecs.size(); i++) {
				retList.push_back(overlapRecs[i]);
				_finalDistances.push_back(0);
			}
		}
		return;
	}
	int upStreamDist = _minUpstreamDist[dbIdx];
	int downStreamDist = _minDownstreamDist[dbIdx];

	if (abs(upStreamDist) < abs(downStreamDist)) {
		if (tieMode == ContextClosest::FIRST_TIE) {
			retList.push_back(upRecs[0]);
			_finalDistances.push_back(upStreamDist);
		} else if (tieMode == ContextClosest::LAST_TIE) {
			retList.push_back(upRecs[upRecs.size()-1]);
			_finalDistances.push_back(upStreamDist);
		} else {

			for (int i=0; i < (int)upRecs.size(); i++) {
				retList.push_back(upRecs[i]);
				_finalDistances.push_back(upStreamDist);
			}
		}
		return;
	}

	if (abs(downStreamDist) < abs(upStreamDist)) {
		if (tieMode == ContextClosest::FIRST_TIE) {
			retList.push_back(downRecs[0]);
			_finalDistances.push_back(downStreamDist);
		} else if (tieMode == ContextClosest::LAST_TIE) {
			retList.push_back(downRecs[downRecs.size()-1]);
			_finalDistances.push_back(downStreamDist);
		} else {

			for (int i=0; i < (int)downRecs.size(); i++) {
				retList.push_back(downRecs[i]);
				_finalDistances.push_back(downStreamDist);
			}
		}
		return;
	}


	if (downStreamDist == upStreamDist) {
		if (tieMode == ContextClosest::FIRST_TIE) {
			retList.push_back(upRecs[0]);
			_finalDistances.push_back(upStreamDist);
		} else if (tieMode == ContextClosest::LAST_TIE) {
			retList.push_back(downRecs[downRecs.size()-1]);
			_finalDistances.push_back(downStreamDist);
		} else {

			for (int i=0; i < (int)upRecs.size(); i++) {
				retList.push_back(upRecs[i]);
				_finalDistances.push_back(upStreamDist);
			}
			for (int i=0; i < (int)downRecs.size(); i++) {
				retList.push_back(downRecs[i]);
				_finalDistances.push_back(downStreamDist);
			}
		}
		return;
	}

}

void CloseSweep::checkMultiDbs(RecordKeyVector &retList) {
	ContextClosest::tieModeType tieMode = _context->getTieMode();

	if (_context->getMultiDbMode() == ContextClosest::ALL_DBS && _numDBs > 1) {
		_copyDists.clear();
		_copyRetList.clearAll();
		_copyRetList.setKey(retList.getKey());
		//loop through retList, find min dist
		int minDist = INT_MAX;
		int i = 0;
		for (; i < (int)_finalDistances.size(); i++) {
			if (abs(_finalDistances[i]) < minDist) {
				minDist = abs(_finalDistances[i]);
			}
		}
		i=0;
		for (RecordKeyVector::const_iterator_type iter = retList.begin(); iter != retList.end(); iter++) {
			int dist = _finalDistances[i];
			if (abs(dist) == minDist) {
				_copyDists.push_back(dist);
				_copyRetList.push_back(*iter);
			}
			i++;
		}

		retList.clearVector();
		_finalDistances.clear();

		if (_copyRetList.empty()) return;

		if (tieMode == ContextClosest::FIRST_TIE) {
			retList.push_back(*(_copyRetList.begin()));
			_finalDistances.push_back(_copyDists[0]);
		} else if (tieMode == ContextClosest::LAST_TIE) {
			retList.push_back(*(_copyRetList.begin() + _copyRetList.size() -1));
			_finalDistances.push_back(_copyDists[_copyDists.size()-1]);
		} else {

			retList = _copyRetList;
			_finalDistances = _copyDists;
		}
	}
}

bool CloseSweep::chromChange(int dbIdx, RecordKeyVector &retList, bool wantScan)
{
	const Record *dbRec = _currDbRecs[dbIdx];

	bool haveQuery = _currQueryRec != NULL;
	bool haveDB = dbRec != NULL;
	\
	if (haveQuery && _currQueryChromName != _prevQueryChromName) {
		_context->testNameConventions(_currQueryRec);
		testChromOrder(_currQueryRec);
	}

	if (haveDB) {
		_context->testNameConventions(dbRec);
		testChromOrder(dbRec);
	}

    // the files are on the same chrom
	if (haveQuery && (!haveDB || _currQueryRec->sameChrom(dbRec))) {

		//if this is the first time the query's chrom is ahead of the chrom that was in this cache,
		//then we have to clear the cache.
		if (!_caches[dbIdx].empty() && queryChromAfterDbRec(_caches[dbIdx].begin()->value())) {
			clearCache(dbIdx);
			_maxPrevLeftClosestEndPos[dbIdx] = 0;
		}
		return false;
	}

	if (haveDB && haveQuery && dbRecAfterQueryChrom(dbRec) && (!_caches[dbIdx].empty() && (_caches[dbIdx].begin()->value()->sameChrom(_currQueryRec)))) {
		//the newest DB record's chrom is ahead of the query, but the cache still
		//has old records on that query's chrom
		scanCache(dbIdx, retList);
		finalizeSelections(dbIdx, retList);
		return true;
	}

	if (!haveQuery || !haveDB) return false;

	// the query is ahead of the database. fast-forward the database to catch-up.
	if (queryChromAfterDbRec(dbRec)) {

		while (dbRec != NULL &&
				queryChromAfterDbRec(dbRec)) {
			_dbFRMs[dbIdx]->deleteRecord(dbRec);
			nextRecord(false, dbIdx);
		}
		clearCache(dbIdx);
		_maxPrevLeftClosestEndPos[dbIdx] = 0;
        return false;
    }
    // the database is ahead of the query.
    else {
        // 1. scan the cache for remaining hits on the query's current chrom.
		if (wantScan) scanCache(dbIdx, retList);

        return true;
    }

	//control can't reach here, but compiler still wants a return statement.
	return true;
}


bool CloseSweep::dbRecAfterQueryChrom(const Record *dbRec)
{
	//If using a genome file, compare chrom ids.
	//Otherwise, compare global order, inserting as needed.
	if (_context->hasGenomeFile()) {
		return ( dbRec->getChromId() > _currQueryRec->getChromId() ) ;
	}
	//see if the db has both it's curr chrom and the query's curr chrom.
	const _orderTrackType *track = _fileTracks[dbRec->getFileIdx()];
	_orderTrackType::const_iterator iter = track->find(dbRec->getChrName());
	if (iter == track->end()) return false; //db file does not contain the curr chrom
	int dbOrder = iter->second;
	iter = track->find(_currQueryRec->getChrName());
	if (iter == track->end()) return false; //db file does not contain the query chrom.
	int qOrder = iter->second;

	return (dbOrder > qOrder);
}

