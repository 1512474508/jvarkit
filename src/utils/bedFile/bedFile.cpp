#include "bedFile.h"
static int binOffsetsExtended[] =
	{4096+512+64+8+1, 512+64+8+1, 64+8+1, 8+1, 1, 0};
static int binOffsets[] = {512+64+8+1, 64+8+1, 8+1, 1, 0};
#define _binFirstShift 17/* How much to shift to get to finest bin. */
#define _binNextShift 3/* How much to shift to get to next larger bin. */

//***********************************************
// Common functions
//***********************************************

void Tokenize(const string& str, vector<string>& tokens)
{
    // Skip delimiters at beginning.
    string::size_type lastPos = str.find_first_not_of("\t", 0);
    // Find first "non-delimiter".
    string::size_type pos     = str.find_first_of("\t", lastPos);

    while (string::npos != pos || string::npos != lastPos)
    {
        // Found a token, add it to the vector.
        tokens.push_back(str.substr(lastPos, pos - lastPos));
        // Skip delimiters.  Note the "not_of"
        lastPos = str.find_first_not_of("\t", pos);
        // Find next "non-delimiter"
        pos = str.find_first_of("\t", lastPos);
    }
}

int overlaps(const int aS, const int aE, const int bS, const int bE) {
	return min(aE, bE) - max(aS, bS);
}

bool leftOf(const int a, const int b) {
	return (a < b);
}

int min(const int a, int b) {
	if (a <= b) {
		return a;
	}
	else {
		return b;
	}
}

int max(const int a, int b) {
	if (a >= b) {
		return a;
	}
	else {
		return b;
	}
}

//*********************************************
// Sorting functions
//*********************************************
bool sortByChrom(BED const & a, BED const & b){
	if (a.chrom < b.chrom) return true;
	else return false;
};

bool sortByStart(const BED &a, const BED &b){
	if (a.start < b.start) return true;
	else return false;
};

bool byChromThenStart(BED const & a, BED const & b){

	if (a.chrom < b.chrom) return true;
	else if (a.chrom > b.chrom) return false;

	if (a.start < b.start) return true;
	else if (a.start >= b.start) return false;

};

//************************************************
// Exception checking
//************************************************

static int getBin(int start, int end)
/* Given start,end in chromosome coordinates assign it
	* a bin.   There's a bin for each 128k segment, for each
	* 1M segment, for each 8M segment, for each 64M segment,
	* and for each chromosome (which is assumed to be less than
	* 512M.)  A range goes into the smallest bin it will fit in. */
{
	int startBin = start, endBin = end-1, i;
	startBin >>= _binFirstShift;
	endBin >>= _binFirstShift;
	for (i=0; i<6; ++i)
	{
		if (startBin == endBin) {
			return binOffsetsExtended[i] + startBin;
		}
		startBin >>= _binNextShift;
		endBin >>= _binNextShift;
	}
	cerr << "start " << start << ", end " << end << " out of range in findBin (max is 512M)" << endl;
	return 0;
}



void BedFile::binKeeperFind(map<int, vector<BED>, std::less<int> > &bk, const int start, const int end, vector<BED> &hits)
/* Return a list of all items in binKeeper that intersect range.
	* Free this list with slFreeList. */
{
	int startBin, endBin;
	int i,j;

	startBin = (start>>_binFirstShift);
	endBin = ((end-1)>>_binFirstShift);
	for (i=0; i<6; ++i)
	{
		int offset = binOffsetsExtended[i];

		for (j=(startBin+offset); j<=(endBin+offset); ++j)  
		{
			for (vector<BED>::iterator el = bk[j].begin(); el != bk[j].end(); ++el) {
				{
					//if (leftOf(end, el->start)) {break;}
					if (overlaps(el->start, el->end, start, end) > 0)
					{
						hits.push_back(*el);
					}
				}
			}
		}
		startBin >>= _binNextShift;
		endBin >>= _binNextShift;
	}
}


void BedFile::countHits(map<int, vector<BED>, std::less<int> > &bk, const int start, const int end)
/* Return a list of all items in binKeeper that intersect range.
	* Free this list with slFreeList. */
{
	int startBin, endBin;
	int i,j;

	startBin = (start>>_binFirstShift);
	endBin = ((end-1)>>_binFirstShift);
	for (i=0; i<6; ++i)
	{
		int offset = binOffsetsExtended[i];

		for (j=(startBin+offset); j<=(endBin+offset); ++j)  
		{
			for (vector<BED>::iterator el = bk[j].begin(); el != bk[j].end(); ++el) {
				{
					//if (leftOf(end, el->start)) {break;}
					if (overlaps(el->start, el->end, start, end) > 0)
					{
						el->count++;
					}
				}
			}
		}
		startBin >>= _binNextShift;
		endBin >>= _binNextShift;
	}
}




// Constructor
BedFile::BedFile(string &bedFile) {
	this->bedFile = bedFile;
}

// Destructorc
BedFile::~BedFile(void) {
}


bool BedFile::parseBedLine (BED &bed, const vector<string> &lineVector, const int &lineNum) {
	
	if ((lineNum == 1) && (lineVector.size() >= 3)) {
		this->bedType = lineVector.size();
		
		if (this->bedType == 3) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			return true;
		}
		else if (this->bedType == 4) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			return true;
		}
		else if (this->bedType ==5) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			bed.score = atoi(lineVector[4].c_str());
			return true;			
		}
		else if (this->bedType == 6) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			bed.score = atoi(lineVector[4].c_str());
			bed.strand = lineVector[5];
			return true;
		}
	}
	else if ( (lineNum > 1) && (lineVector.size() == this->bedType)) {
		
		if (this->bedType == 3) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			return true;
		}
		else if (this->bedType == 4) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			return true;
		}
		else if (this->bedType ==5) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			bed.score = atoi(lineVector[4].c_str());
			return true;			
		}
		else if (this->bedType == 6) {
			bed.chrom = lineVector[0];
			bed.start = atoi(lineVector[1].c_str());
			bed.end = atoi(lineVector[2].c_str());
			bed.name = lineVector[3];
			bed.score = atoi(lineVector[4].c_str());
			bed.strand = lineVector[5];
			return true;
		}
		
		if (bed.start > bed.end) {
			cout << "Error: malformed BED entry at line " << lineNum << ". Start was greater than End. Ignoring it and moving on." << endl;
			return false;
		}
		else if ( (bed.start < 0) || (bed.end < 0) ) {
			cout << "Error: malformed BED entry at line " << lineNum << ". Coordinate <= 0. Ignoring it and moving on." << endl;
			return false;
		}
	}
	else if (lineVector.size() != this->bedType) {
		cerr << "Differing number of BED fields encountered at line: " << lineNum << ".  Exiting" << endl;
		exit(1);
	}
	else if (lineVector.size() < 3) {
		cerr << "TAB delimited BED file with at least 3 fields (chrom, start, end) is required.  Exiting" << endl;
		exit(1);
	}
}


void BedFile::loadBedFileIntoMap() {

	// open the BED file for reading                                                                                                                                      
	ifstream bed(bedFile.c_str(), ios::in);
	if ( !bed ) {
		cerr << "Error: The requested bed file (" <<bedFile << ") could not be opened. Exiting!" << endl;
		exit (1);
	}

	string bedLine;
	BED bedEntry;                                                                                                                        
	int lineNum = 0;

	//while (bed >> bedEntry.chrom >> bedEntry.start >> bedEntry.end) {
	while (getline(bed, bedLine)) {
		
		vector<string> bedFields;
		Tokenize(bedLine,bedFields);

		lineNum++;

		if (parseBedLine(bedEntry, bedFields, lineNum)) {
			int bin = getBin(bedEntry.start, bedEntry.end);
			bedEntry.count = 0;
			this->bedMap[bedEntry.chrom][bin].push_back(bedEntry);	
		}
	}
}

void BedFile::loadBedFileIntoMapNoBin() {

	// open the BED file for reading                                                                                                                                      
	ifstream bed(bedFile.c_str(), ios::in);
	if ( !bed ) {
		cerr << "Error: The requested bed file (" <<bedFile << ") could not be opened. Exiting!" << endl;
		exit (1);
	}

	string bedLine;
	BED bedEntry;                                                                                                                        
	int lineNum = 0;

	while (getline(bed, bedLine)) {
		
		vector<string> bedFields;
		Tokenize(bedLine,bedFields);

		lineNum++;

		if (parseBedLine(bedEntry, bedFields, lineNum)) {
			bedEntry.count = 0;
			this->bedMapNoBin[bedEntry.chrom].push_back(bedEntry);	
		}
	}

	// sort the BED entries for each chromosome
	// in ascending order of start position
	for (masterBedMapNoBin::iterator m = this->bedMapNoBin.begin(); m != this->bedMapNoBin.end(); ++m) {
		sort(m->second.begin(), m->second.end(), sortByStart);		
	}
}




