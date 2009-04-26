#include "closestBed.h"

using namespace std;

// define our program name
#define PROGRAM_NAME "closestBed"

// define the version
#define VERSION "1.1.0"

// define our parameter checking macro
#define PARAMETER_CHECK(param, paramLen, actualLen) (strncmp(argv[i], param, min(actualLen, paramLen))== 0) && (actualLen == paramLen)

// function declarations
void ShowHelp(void);

int main(int argc, char* argv[]) {

	// our configuration variables
	bool showHelp = false;

	// input files
	string bedAFile;
	string bedBFile;

	bool haveBedA = false;
	bool haveBedB = false;

	// check to see if we should print out some help
	if(argc <= 1) showHelp = true;

	for(int i = 1; i < argc; i++) {
		int parameterLength = (int)strlen(argv[i]);

		if(PARAMETER_CHECK("-h", 2, parameterLength) || 
		PARAMETER_CHECK("--help", 5, parameterLength)) {
			showHelp = true;
		}
	}

	if(showHelp) ShowHelp();

	// do some parsing (all of these parameters require 2 strings)
	for(int i = 1; i < argc; i++) {

		int parameterLength = (int)strlen(argv[i]);

		if(PARAMETER_CHECK("-a", 2, parameterLength)) {
			haveBedA = true;
			bedAFile = argv[i + 1];
			i++;
		}
		else if(PARAMETER_CHECK("-b", 2, parameterLength)) {
			haveBedB = true;
			bedBFile = argv[i + 1];
			i++;
		}	
	}

	// make sure we have both input files
	if (!haveBedA || !haveBedB) {
		cerr << endl << "*****" << endl << "*****ERROR: Need -a and -b files. " << endl << "*****" << endl;
		showHelp = true;
	}

	if (!showHelp) {
		BedClosest *bc = new BedClosest(bedAFile, bedBFile);
		bc->ClosestBed();
		return 0;
	}
	else {
		ShowHelp();
	}
}

void ShowHelp(void) {

	cerr << "===============================================" << endl;
	cerr << " " <<PROGRAM_NAME << " v" << VERSION << endl ;
	cerr << " Aaron Quinlan, Ph.D. (aaronquinlan@gmail.com)  " << endl ;
	cerr << " Hall Laboratory, University of Virginia" << endl;
	cerr << "===============================================" << endl << endl;
	cerr << "Description: For each feature in BED A, finds the closest feature (upstream or downstream) in BED B" << endl;

	cerr << "***NOTE: Only BED3 - BED6 formats allowed.***"<< endl << endl;

	cerr << "Usage: " << PROGRAM_NAME << " [OPTIONS] -a <a.bed> -b <b.bed>" << endl << endl;

	cerr << "NOTES: " << endl;
	cerr << "\t" << "-i stdin " << "allows closestBed to read BED A from stdin.  E.g.: cat a.bed | closestBed -a stdin -b B.bed" << endl << endl;
	cerr << "\t" << "Reports \"none\" for chrom and \"-1\" for all other fields when a feature is not found in B on the same chromosome as the feature in A.  E.g. none -1 -1" << endl;

	// end the program here
	exit(1);

}
