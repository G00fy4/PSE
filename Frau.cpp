#include "Frau.h"
#include "Mensch.h"
using namespace std;

Frau::Frau(string name) : Mensch(name){
	ehemann = 0;
	
	for (int i = 0; i < 10; i++){
	kinder[i] = 0;
	}	
}


Frau::~Frau(){

}

void Frau::kind(int kindNr, string kindName){
	// Mensch kind(kindName)
	Mensch* kind = new Mensch(kindName);
	kinder[kindNr] = kind;
}

void Frau::heirat(string mannName){
	Mensch* mann = new Mensch(mannName);
	ehemann = mann;
}

void Frau::stirbtKind(int kindNr){
	Mensch* kind = kinder[kindNr];
	delete kind;
	kinder[kindNr] = 0;
}

void Frau::stirbtEhemann(void){
	Mensch* mann = ehemann;
	delete mann;
	ehemann = 0;
}

void Frau::zeige(void){
	if (ehemann != 0){
	cout << "Name des Ehemannes: " << ehemann->getName() << endl;
	}
	
	for (int i = 0; i < 10; i++){
		if (kinder[i] != 0){
			cout << "Name des Kindes: " << kinder[i]->getName() << endl;
		}
	}

	cout << "Vermoegen: " << vermoegen << endl << endl;
}