import sys

if len(sys.argv) !=2:
	print('Missed specifying the file name.');
	sys.exit();

file=sys.argv[1];
feat='';
fscore=0.0;
lscore=0.0;
prev='';
count=0;
with open(file) as f:
	for line in f:
		line=line.strip();
		if line.startswith('('):
			pprev=prev;
			prev=line;
			if count>0:
				lscore=lscore/count;
				if lscore>fscore:
					feat=pprev;
					fscore=lscore;
			count=0;
			lscore=0.0;
		elif line.startswith('B-SNE'):			
			count+=1;
			lscore+=float(line.split()[3]);
if count>0:
	lscore=lscore/count;
	if lscore>fscore:
		feat=prev;
		fscore=lscore;

print feat;
print fscore;