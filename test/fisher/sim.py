import sys
from random import randint
from subprocess import check_output

genome_size = 25000000
nA = 210
nB = 3390

minA, maxA = (20, 2000)
minB, maxB = (20, 1250)

for f, imin, imax, n in (
        ('taa.bed', minA, maxA, nA),
        ('tbb.bed', minB, maxB, nB)):
    with open(f, 'w') as fh:
        vals = []
        for i in range(n):
            s = randint(0, genome_size - imax)
            e = randint(s + imin, min(genome_size, s + imax))
            vals.append((s, e))
        for s, e in sorted(vals):
            fh.write("chr1\t%i\t%i\n" % (s, e))
        fh.flush()

print >> open('tgg.genome', 'w'), ("chr1\t%i" % genome_size)

print check_output("../../bin/bedtools fisher -a taa.bed -b tbb.bed -g tgg.genome", shell=True)
