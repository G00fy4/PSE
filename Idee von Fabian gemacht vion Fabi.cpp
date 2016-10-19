// versuch von Fabian Fabi


#include <iostream>
using namespace std;

 int main (void) {

	 int a =0;
	 int b =0;
	 int c =0;
	 char v;

	 cin >> a >> v;

	 switch (v){
	 case '+': 
		 cin >>b;
		 cout <<a+b<<"\n";
		 break;

	 case '-': 
		 cin >>b;
		 cout <<a-b<<"\n";
		 break;

	 case '*': 
		 cin >>b;
		 cout <<a*b<<"\n";
		 break;

	 case '/': 
		 cin >>b;
		 cout <<a/b<<"\n";
		 break;

	 case '^': 
		 cin >>b;
		 while (b!=1){
		//if (b!=1){
			 c=a*a;
		 b=b-1;
		
		 }     //^ while
		 cout <<c<<"\n";
		 break;

	 default :
		 cout << "das kann ich nicht du Fickschnitzel!\n";
		 break;

	


	 }   //switch
	 system ("pause");

	 return 0;

 }   // main 