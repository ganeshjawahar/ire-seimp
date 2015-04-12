from itertools import chain
import nltk
from sklearn.metrics import classification_report,confusion_matrix
from sklearn.preprocessing import LabelBinarizer
import sklearn
import pycrfsuite
import random
from collections import Counter
import sys
import itertools

def genDataset(file,trainSize):
	"""
	Generates the training and testing set in random fashion.
	"""
	#Read the entire content
	tweets=[];
	with open(file) as f:
		tweets=f.readlines();

	#Randomly shuffle the tweets
	random.shuffle(tweets);

	#Load the train and test list.
	train_sents=[];
	test_sents=[];
	for i in range(trainSize):
		train_sents.append(tweets[i]);
	for i in range(len(tweets)-trainSize):
		test_sents.append(tweets[trainSize+i]);

	return (train_sents,test_sents);

def word2features(sent,i,options,subset):
	features=[];
	for element in subset:
		features.append(element+'='+str(options[element](sent)));

	if i>0:
		for element in subset:
			features.append('-1:'+element+'='+str(options[element](sent)));
	else:
		features.append('BOS');

	if i < len(sent)-1:
		for element in subset:
			features.append('+1:'+element+'='+str(options[element](sent)));
	else:
		features.append('EOS');

	return features

def sent2features(sent,options,subset):
	return [word2features(sent[i],i,options,subset) for i in range(len(sent))]

def sent2labels(sent):
	label=[];
	for tok in sent:
		split=tok.split('$$$');
		label.append(split[5]);
	return label;

def tweet2Tokens(tweet):
	tokens=[];
	split=tweet.split('$$$');
	content='';
	for i in range(len(split)):
		if i!=0 and i%6==0:
			tokens.append(content);
			content=split[i];
		elif i==0:
			content+=split[i];
		else:
			content+='$$$'+split[i];			

	return tokens;

def countSNE(set):
	count=0;
	for sent in set:
		for line in tweet2Tokens(sent):
			if line.split('$$$')[4]=='SNE':
				count=count+1;
	return count;

def countPredictedSNE(set):
	count=0;
	for oentry in set:
		for entry in oentry:
			if entry=='I-SNE':
				count+=1;
	return count;


def bio_classification_report(y_true, y_pred):
    """
    Classification report for a list of BIO-encoded sequences.
    It computes token-level metrics and discards "O" labels.
    
    Note that it requires scikit-learn 0.15+ (or a version from github master)
    to calculate averages properly!
    """
    lb = LabelBinarizer()
    y_true_combined = lb.fit_transform(list(chain.from_iterable(y_true)))
    y_pred_combined = lb.transform(list(chain.from_iterable(y_pred)))
        
    tagset = set(lb.classes_) - {'O'}
    tagset = sorted(tagset, key=lambda tag: tag.split('-', 1)[::-1])
    class_indices = {cls: idx for idx, cls in enumerate(lb.classes_)}
    
    return classification_report(
        y_true_combined,
        y_pred_combined,
        labels = [class_indices[cls] for cls in tagset],
        target_names = tagset,
    )

def print_transitions(trans_features):
	for (label_from, label_to), weight in trans_features:
		print("%-6s -> %-7s %0.6f" % (label_from, label_to, weight))

def print_state_features(state_features):
	for (attr, label), weight in state_features:
		print("%0.6f %-6s %s" % (weight, label, attr))

def identify_salient_ne(train_size,options,subset):
	[train_sents,test_sents]=genDataset('CricketWorldCup2015Dataset_B.txt',train_size);

	X_train=[sent2features(tweet2Tokens(s),options,subset) for s in train_sents]
	y_train=[sent2labels(tweet2Tokens(s)) for s in train_sents]

	X_test=[sent2features(tweet2Tokens(s),options,subset) for s in test_sents]
	y_test=[sent2labels(tweet2Tokens(s)) for s in test_sents]

	#print 'Training Set Size = '+str(len(train_sents));
	#print 'Testing Set Size = '+str(len(test_sents));
	#print 'SNE instances in Train = '+str(countSNE(train_sents));
	#print 'SNE instances in Test = '+str(countSNE(test_sents));

	trainer = pycrfsuite.Trainer(verbose=False)
	#print trainer.params();

	for xseq, yseq in zip(X_train, y_train):
		trainer.append(xseq, yseq)

	trainer.set_params(
	{
		'c1': 1.0,   # coefficient for L1 penalty
		'c2': 1e-3,  # coefficient for L2 penalty
		'max_iterations': 50,  # stop earlier

		# include transitions that are possible, but not observed
		'feature.possible_transitions': True
	});

	trainer.train('CricketWorldCup2015Dataset.crfsuite');

	tagger = pycrfsuite.Tagger();
	tagger.open('CricketWorldCup2015Dataset.crfsuite');

	y_pred = [tagger.tag(xseq) for xseq in X_test]

	#print "SNE instances predicted ="+str(countPredictedSNE(y_pred));
	print(bio_classification_report(y_test, y_pred))

	info = tagger.info();
	#print("Top likely transitions:")
	#print_transitions(Counter(info.transitions).most_common(15))

	#print("Top positive:")
	#print_state_features(Counter(info.state_features).most_common(20))

	#print("\nTop negative:")	
	#print_state_features(Counter(info.state_features).most_common()[-20:])

def lower(str):
	word=str.split('$$$')[0];
	return word.lower();

def upper(str):
	word=str.split('$$$')[0];
	return word.upper();

def isTitle(str):
	word=str.split('$$$')[0];
	return word.isTitle();

def isDigit(str):
	word=str.split('$$$')[0];
	return word.isDigit();

def postag(str):
	pos=str.split('$$$')[2];
	return pos;

def entity(str):
	entity=str.split('$$$')[1];
	return entity;

def chunk(str):
	chunk=str.split('$$$')[3];
	return chunk;

def isUpper(str):	
	word=str.split('$$$')[0];
	return word.isupper();

def isLower(str):
	word=str.split('$$$')[0];
	return word.islower();

def isFirstCharHash(str):
	word=str.split('$$$')[0];
	return (len(word)>0 and word[0]=='#'); 

def isFirstCharHashOrAt(str):
	word=str.split('$$$')[0];
	return (len(word)>0 and (word[0]=='#' or word[0]=='@'));

def isFirstCharCaps(str):
	word=str.split('$$$')[0];
	return (len(word)>0 and word[0].isupper());

def isEntity(str):
	entity=str.split('$$$')[1];
	return (entity=='B-ENTITY' or entity=='I-ENTITY');

def isStartsWithNN(str):
	pos=str.split('$$$')[2];
	return (pos.startswith('NN'));

def isStartsWithNNOrPR(str):
	pos=str.split('$$$')[2];
	return (pos.startswith('NN') or pos.startswith('PR'));

def main(options,subset):
	if len(sys.argv)!=3:
		print("Missing arguments.\nFORMAT : python seimp.py <trainSize> <numIterations>");
		sys.exit();
	trainSize=int(sys.argv[1]);
	numIterations=int(sys.argv[2]);
	for i in range(numIterations):
		identify_salient_ne(trainSize,options,subset);

def featureInit():
	#'lower':lower,'upper':upper,'isUpper':isUpper,'isLower':isLower,
	options={'postag':postag,'entity':entity,'chunk':chunk,'isFirstCharHash':isFirstCharHash,'isFirstCharHashOrAt':isFirstCharHashOrAt,'isFirstCharCaps':isFirstCharCaps,'isEntity':isEntity,'isStartsWithNN':isStartsWithNN,'isStartsWithNNOrPR':isStartsWithNNOrPR};
	count=0;
	for L in range(0, len(options)+1):
		for subset in itertools.combinations(options,L):
			if len(subset)>0:
				print subset;
				main(options,subset);
			count+=1;
			print count;

featureInit();