import sys

if len(sys.argv) !=2:
	print('Missed specifying the file name.');
	sys.exit();

file=sys.argv[1];
lines=[];
with open(file) as f:
	lines=f.readlines();

res_isne=[0]*3;
res_osne=[0]*3;
res_avg=[0]*3;
numIterations=0;
for line in lines:
	line=line.strip();
	if len(line)!=0:
		if line.startswith('I-SNE'):
			split=line.split();
			res_isne[0]=res_isne[0]+float(split[1]);
			res_isne[1]=res_isne[1]+float(split[2]);
			res_isne[2]=res_isne[2]+float(split[3]);
			numIterations+=1;
		elif line.startswith('O-SNE'):
			split=line.split();
			res_osne[0]=res_osne[0]+float(split[1]);
			res_osne[1]=res_osne[1]+float(split[2]);
			res_osne[2]=res_osne[2]+float(split[3]);
		elif line.startswith('avg'):
			split=line.split();
			res_avg[0]=res_avg[0]+float(split[3]);
			res_avg[1]=res_avg[1]+float(split[4]);
			res_avg[2]=res_avg[2]+float(split[5]);

#Normalize the values
for i in range(3):
	res_isne[i]=(res_isne[i]/numIterations);

for i in range(3):
	res_osne[i]=(res_osne[i]/numIterations);

for i in range(3):
	res_avg[i]=(res_avg[i]/numIterations);

print "# Iterations ="+str(numIterations);
print '\tP\tR\tF'
print 'I-SNE\t'+str(res_isne[0])+'\t'+str(res_isne[1])+'\t'+str(res_isne[2]);
print 'O-SNE\t'+str(res_osne[0])+'\t'+str(res_osne[1])+'\t'+str(res_osne[2]);
print 'AVG\t'+str(res_avg[0])+'\t'+str(res_avg[1])+'\t'+str(res_avg[2]);