actor Sender extends Receiver (0) {
	knownactors {
		Medium medium;
		Receiver rec;
		int rec;
	}

	actorvars {
		string receivedBit;
		string rec;
		ActorType1 m;
	}

	msghandler initial(int a) {
        int hasSucceeded;
        string hasSucceeded;
        a = 1 - -2;
        d = a+b*zz-dd*5;
        medium.pass(sendBit);
        self.sendMsg(1,2);
    }

	msghandler sendMeseg() {
		//receive(hasSucceeded)++++
		if (hasSucceeded == a) {
			if (sendBit == b){
				sendBit = 1;
			}
			else{
				sendBit = 2;
			}
		}

		selfffff.sendMsg();
	}


}

actor Receiver extends Sender (5){
	knownactors {
		Medium medium;
		Sender _sender;
	}

	actorvars {
	}

    msghandler sendMsg() {
        int bah;
        if(bah== bah){
            print("yaaaaay");
        }
    }

	msghandler receiveMsg(boolean msgBit) {
		messageBit =msgBit;
		//sender.receive(true);
	}
}

actor Medium(5) {
	knownactors {
		Receiver receiver;
		Sender _sender;
	}

	actorvars {
		boolean passMessage;
		int messageBit;
	}

	msghandler initial() {
		int messageBit;
		passMessage=true;
		print(a==b);
	}

	msghandler pass(boolean msgBit) {
	    if(1==2){}
        else {
            if(passMessage == true) {
                receiver.receiveMsg( msgBit);
            } else {
                //sender,receive(false);
            }
        }
	}
}

actor A (1){
    knownactors{}
    actorvars{}
}

actor A extends B (1){
    knownactors{}
    actorvars{}
}

actor B extends A (1){
    knownactors{}
    actorvars{}
}

main {
	Sender b (medium, receiver):();
	Medium b (receiver, _sender):();
	Receiver receiver(medium, _sender):(2+3, 2 - -a, f == g == h);
}