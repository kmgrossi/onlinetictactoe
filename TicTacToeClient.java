import java.io.*;
import java.util.*;
import java.net.Socket;

public class TicTacToeClient 
{
	public static void main(String[] args) throws Exception
	{
		try (Socket socket = new Socket("codebank.xyz", 38006))
		{
			OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
			InputStream is = socket.getInputStream();
            ObjectInputStream messageListener = new ObjectInputStream(is);
            boolean inProgress = true;            

			//Creating a Listener Thread to see if any Error Messages are encountered.
            Runnable listener = new Runnable()
            {
                public void run()
                {
                    try
                    {
                        System.out.println("After messageListener.");

                        while(true)
                        {	
                            Message message = (Message)messageListener.readObject();

                            if(message == null)
                                return;

                            switch(message.getType())
                            {

                            case ERROR:
                            	System.out.println("Received Error Message...");
                            	ErrorMessage em = (ErrorMessage)message;
                            	System.out.println(em.getError());
                            	break;

                            case BOARD:
                            	System.out.println("Retrieving Board State...");
                            	BoardMessage bm = (BoardMessage)message;

                            	switch (bm.getStatus())
                            	{
                            		case PLAYER1_SURRENDER:
                            			System.out.println(bm.getStatus().toString());
                            			break;

                            		case PLAYER2_SURRENDER:
                            			System.out.println(bm.getStatus().toString());
                            			break;

                            		case PLAYER1_VICTORY:
                            			byte[][] board = bm.getBoard();
                            			System.out.println(bm.getStatus().toString());
                            			printBoard(board);
                            			break;

                            		case PLAYER2_VICTORY:
                            			board = bm.getBoard();
                            			System.out.println(bm.getStatus().toString());
                            			printBoard(board);
                            			break;

                            		case STALEMATE:
                            			board = bm.getBoard();
                            			System.out.println(bm.getStatus().toString());
                            			printBoard(board);
                            			break;

                            		case IN_PROGRESS:
	                            		board = bm.getBoard();
	                					byte bmTurn = bm.getTurn();
	                					System.out.println("This is the current turn # : " + bmTurn);
	                					printBoard(board);
	                            		break;

                            		case ERROR:
                            			break;
                            	}
            				   	break;

                            default:
                            	System.out.println("No known message encountered.");
                            	break;
                            }//end switch                    
                        }
                    }catch(Exception e){e.printStackTrace();}
                }//end run
            }; //end runnable

            //Starting listener thread.
            new Thread(listener).start();
       
            System.out.println("Connecting to server...");

            //First we send a connect message identifying ourselves as the user "Kevin".
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter a name for Player 1 : ");
            String name = sc.nextLine();
            ConnectMessage connectMessage = new ConnectMessage(name);
            System.out.println();            

            //We then write the serialized connect message object. 
            oos.writeObject(connectMessage);
            
            //Creating a Command Message with the enum NEW_GAME to let the server know that 
            //I want to start a new game. 
            CommandMessage cm = null;
            cm = new CommandMessage(CommandMessage.Command.NEW_GAME);
            oos.writeObject(cm);
            

            while(inProgress)
            {
            	Thread.sleep(500);

            	CommandMessage sentCommand = null;
            	MoveMessage sentMove = null;

            	System.out.println("Please make a decision using the number associated with the action.");
            	Scanner in = new Scanner(System.in);
            	System.out.print("[1] COMMAND  \n[2]  MOVE  \n");
            	int response = in.nextInt();

            	//User has chosen command.
            	if(response == 1)
            	{
            		System.out.println("Please make a decision using the number associated with the action.");
            		System.out.print("[1] NEW_GAME \n[2] LIST_PLAYERS  \n[3] EXIT  \n[4] SURRENDER \n");
            		response = in.nextInt();

            		switch(response)
            		{
            			case 1:            				
            				sentCommand = new CommandMessage(CommandMessage.Command.NEW_GAME);
            				oos.writeObject(sentCommand);
            				break;

            			case 2:            				
            				sentCommand = new CommandMessage(CommandMessage.Command.LIST_PLAYERS);
            				oos.writeObject(sentCommand);
            				break;

            			case 3:
            				sentCommand = new CommandMessage(CommandMessage.Command.EXIT);
            				oos.writeObject(sentCommand);
            				inProgress = false;
            				break;

            			case 4:
            				sentCommand = new CommandMessage(CommandMessage.Command.SURRENDER);
            				oos.writeObject(sentCommand);
            				break;

            			default:
            				System.out.println("Invalid Input!");
            				break;
            		}

            	}//end command response

            	//User has chosen move.
            	else if(response == 2)
            	{
            		System.out.println("Please select a row # ");
            		byte row = (byte)(in.nextInt() - 1);
            		System.out.println();
            		System.out.println("Please select a col # ");
            		byte col = (byte)(in.nextInt() - 1);
            		System.out.println();

            		sentMove = new MoveMessage(row, col);
            		oos.writeObject(sentMove);

            	}//end move response

            	else
            	{
            		System.out.println("Invalid input!");
            	}
            }//end while

		}//end Try
	}//end main

	public static void printBoard(byte[][] board)
	{
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				if(j != 2)  
					switch (board[i][j])
					{
						case 0:
							System.out.print(" " + " " + "|" + " ");
							break;

						case 1:
							System.out.print("X" + " " + "|" + " ");
							break;

						case 2:
							System.out.print("O" + " " + "|" + " ");
							break;
					}          							
					   
				else
					switch (board[i][j])
					{
						case 0:
							System.out.print(" ");
							break;

						case 1:
							System.out.print("X");
							break;

						case 2:
							System.out.print("O");
							break;
					} 					   	            				
			}
			System.out.println();
			if(i != 2)
				System.out.print("----------  \n");		
		}
		System.out.println();
	}
}//end class