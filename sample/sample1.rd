actor Calculator(5){
	knownactors{
	}
	actorvars{
		int result;
		
	}
	msghandler initial(int balance)
	{
		result=0;
	}

	msghandler sum(int a, int b){
		result = a + b;
		print(result);
	}
}




actor User(2){
	knownactors{
		Calculator c;
	
	}
	actorvars{
		string name;
		int numOfRequests;
		
	}
	msghandler initial(int a, int b)
	{
		numOfRequests = 0;
		self.get_sum(a,b);
	}

	msghandler get_sum(int a, int b){
		if(numOfRequests == 0){
			c.sum(a,b);
			numOfRequests = numOfRequests+1;
		}
		else{
			print("Can't send new request");
		}
	}

}


main {
	Calculator C():();
	User user(C):(2,3);
}



















