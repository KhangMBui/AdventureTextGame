/*
 * File: Adventure.java
 * --------------------
 * This program plays the Adventure game from Assignment #4.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/* Class: Adventure */
/**
 * This class is the main program class for the Adventure game.
 */

public class Adventure {

	/**
	 * This method is used only to test the program
	 */
	public static void setScanner(Scanner theScanner) {
		scan = theScanner;

	}

	/**
	 * Runs the adventure program
	 */
	public static void main(String[] args) {
		System.out.print("What will be your adventure today? ");
		String name = scan.nextLine();

		Adventure game = new Adventure();

		// read the room file
		String roomFile = name + "Rooms.txt";
		try {
			Scanner s = new Scanner(new File(roomFile));
			while (s.hasNextInt()) {
				AdvRoom room = AdvRoom.readFromFile(s);
				game.rooms.put(room.getRoomNumber(), room);
			}
		} catch (IOException e) {
			System.out.println("The rooms file '" + roomFile
					+ "' couldn't be read.");
			return;
		}
		if (name.equals("Tiny")) {
			game.run();
		}
		else {
			
			
			// read the object file and place the objects in their corresponding
			// rooms.
			String objectFile = name + "Objects.txt";
			
			try {
				Scanner s = new Scanner(new File(objectFile));
				while (s.hasNextLine()) {
					AdvObject obj = AdvObject.readFromFile(s);
					if (obj != null) {
						game.rooms.get(obj.getInitialLocation()).addObject(obj);
						objectList.put(obj.getName(), obj);
					}
				}
			} catch (IOException e) {
				System.out.println("The objects file '" + objectFile + "' couldn't be read.");
				return;
			}
			
			// Read the synonym file
			String synonymFile = name + "Synonyms.txt";
			try {
				Scanner s = new Scanner(new File(synonymFile));
				String line;
				while (s.hasNextLine() && (line = s.nextLine().trim()).length() > 0) {
					String[] parts = line.split("=");
					game.synonyms.put(parts[0], parts[1]);
				}
			} catch (IOException e) {
				System.out.println("The synonyms file '" + objectFile
						+ "' couldn't be read.");
				return;
			}
			
			// Run the adventure
			game.run();
		}
	}

	// Run the game
	public void run() {
		currentRoom = rooms.get(rooms.firstKey());
		//Description
		String[] des = currentRoom.getDescription();
		for (int i = 0; i<des.length; i++) {
			System.out.println(des[i]);
		}

		// loop
		while (true) {
			// ask for a command
			System.out.print("> ");
			String command = scan.nextLine().trim().toUpperCase();
			// process the command
			// split on one or more spaces: \s+
			String[] parts = command.split("\\s+");
			
			for (int i = 0; i<parts.length; i++) {
				if (synonyms.containsKey(parts[i])) {
					parts[i] = synonyms.get(parts[i]);
				}
			}
			if (parts.length > 0) {
				AdvCommand cmd = null;
				AdvObject obj = null;
				if (parts.length > 1) {
					//ask
					// get the object from parts[1]
					obj = objectList.get(parts[1]);
				}
				switch (parts[0]) {
				case "TAKE":
					// take command
					cmd = AdvCommand.TAKE;
					break;
				case "DROP":
					// drop command
					cmd = AdvCommand.DROP;
					break;
				case "HELP":
					cmd = AdvCommand.HELP;
					break;
				case "LOOK":
					cmd = AdvCommand.LOOK;
					break;
				case "QUIT":
					cmd = AdvCommand.QUIT;
					break;
				case "INVENTORY":
					cmd = AdvCommand.INVENTORY;
					break;
				case "RESTART" :
					cmd = AdvCommand.RESTART;
					break;
				default: // any motion command
					cmd = new AdvMotionCommand(parts[0]);
					break;
				}
				// execute the command
				cmd.execute(this, obj);
				if (endGame) {
					break;
				}
			}
		}
	}

	/* Method: executeMotionCommand(direction) */
	/**
	 * Executes a motion command. This method is called from the
	 * AdvMotionCommand class to move to a new room.
	 * 
	 * @param direction
	 *            The string indicating the direction of motion
	 */
	public void executeMotionCommand(String direction) {
		//if user entered nothing
		if (direction.isEmpty()) {
			return;
		}
		System.out.println("Going " + direction);
		//Get possible direction
		AdvMotionTableEntry[] possibleDirection = this.currentRoom.getMotionTable();
		//Keep track if can go to that direction, using flag
		Boolean flag = true;	
		for (int i = 0; i<possibleDirection.length; i++) {
			if (possibleDirection[i].getDirection().equals(direction)) {
				//else
				if (possibleDirection[i].getKeyName() == null) {
					AdvRoom nextRoom = this.rooms.get(possibleDirection[i].getDestinationRoom());
					transport(nextRoom);
					flag = false;
					break;
				}
				//Room that requires object
				if (this.inventory.contains(objectList.get(possibleDirection[i].getKeyName()))) {
					AdvRoom nextRoom = this.rooms.get(possibleDirection[i].getDestinationRoom());
					transport(nextRoom);
					flag = false;
					break;
					
				}
			}
		}
		//if passed all the cases above, print the sentence out:
		if (flag)
			System.out.println("Couldn't go in that direction.");
	}
	private void transport(AdvRoom nextRoom) {
		//assign current room to next room
		this.currentRoom = nextRoom;
		//print out description if this is the first time in the room
		if (!currentRoom.hasBeenVisited()) {
			this.executeLookCommand();
		}
		//else, just print out room's name
		else {
			System.out.println(currentRoom.getName());
		}
		//If forced, execute force command
		if (currentRoom.getMotionTable()[0].getDirection().equals("FORCED")) {
			executeForcedCommand();
		}
	}

	/* Method: executeQuitCommand() */
	/**
	 * Implements the QUIT command. This command should ask the user to confirm
	 * the quit request and, if so, should exit from the play method. If not,
	 * the program should continue as usual.
	 */
	public void executeQuitCommand() {
		if (!endGame) {
			System.out.print("One that gives up might end up being devoured by the Devourer of Gods. You trivial human being dare quit? (Y/Any keys)\n>");
		}
		else {
			System.out.print("Quit? (Y/AnyKeys)\n>");
		}
		char quit;
		if ( !((quit = Adventure.scan.nextLine().toUpperCase().charAt(0)) == 'Y') ) {
			System.out.println("KEEP THE ONSLAUGHT GOING!!!");
			
		}
		else {
			if (endGame) {
				System.out.print("Game over. Good bye!");
			}
			else {
				System.out.println("You shameful creature. Game over.");
				endGame = true;
			}
		}
	}
	//An extra restart function for the program. However, no synonym
	//is built since I can't (shouldn't) adjust the given txt files.
	public void executeRestartCommand() {
		System.out.print("Restart? (Y/Any key)\n>");
		char restart;
		if ( !((restart = Adventure.scan.nextLine().toUpperCase().charAt(0)) == 'Y') ) {
			System.out.println("KEEP THE ONSLAUGHT GOING!!!");
			return;
		}
		else {
			main(null);
		}
	}

	/* Method: executeHelpCommand() */
	/**
	 * Implements the HELP command. Your code must include some help text for
	 * the user.
	 */
	public void executeHelpCommand() {
		System.out.println("I'm the Devourer of Gods. I'll help you.");
		System.out.println("You may try pressing q to quit, pressing RESTART to restart, or going to following directions:");
		//Get possible directions:
		for (AdvMotionTableEntry t : this.currentRoom.getMotionTable()) {
			String instruction = t.getKeyName()!=null?": " + t.getKeyName():"";
			System.out.println(t.getDirection() + instruction);
		}
		//Print out object in current room if it has any:
		if (this.currentRoom.getObjectCount()>0) {		
			for (int i = 0; i<currentRoom.getObjectCount(); i++) {
				System.out.println("There is " + currentRoom.getObject(i).getDescription() + " in this room.");
			}
		}
	}

	/* Method: executeLookCommand() */
	/**
	 * Implements the LOOK command. This method should give the full description
	 * of the room and its contents.
	 */
	public void executeLookCommand() {
		//System.out.println("Room number " + currentRoom.getRoomNumber());
		System.out.println("We're in " + this.currentRoom.getName());
		//Print out current room's description:
		for (String s : this.currentRoom.getDescription()) {
			System.out.println(s);
		}
		//Iterate through object list and print their descriptions out:
		if (this.currentRoom.getObjectCount()>0) {
			for (int i = 0; i<currentRoom.getObjectCount(); i++) {
				System.out.println("There is " + currentRoom.getObject(i).getDescription() + " in this room.");
			}
		}
		this.currentRoom.setVisited(true);	
	}

	/* Method: executeInventoryCommand() */
	/**
	 * Implements the INVENTORY command. This method should display a list of
	 * what the user is carrying.
	 */
	public void executeInventoryCommand() {
		//If there's no item in your inventory
		if (inventory.size() == 0) {
			System.out.println("Inventory is empty.");
			return;
		}
		System.out.println("Zipping off our backpack...We have:");
		//Iterate through the inventory and print out the names of items:
		for (AdvObject o : inventory) {
			System.out.println(o.getName() + ": " + o.getDescription());
		}
	}

	/* Method: executeTakeCommand(obj) */
	/**
	 * Implements the TAKE command. This method should check that the object is
	 * in the room and deliver a suitable message if not.
	 * 
	 * @param obj
	 *            The AdvObject you want to take
	 */
	public void executeTakeCommand(AdvObject obj) {
		//If that object exist in this game and in this current room.
		if (obj != null && this.currentRoom.containsObject(obj)) {
			//Remove object from current room and add it to your inventory
			this.currentRoom.removeObject(obj);
			this.inventory.add(obj);
			System.out.println("Successfully stole " + obj.getName());
		}
		else {
			System.out.println("Couldn't find that item.");
		}
	}

	/* Method: executeDropCommand(obj) */
	/**
	 * Implements the DROP command. This method should check that the user is
	 * carrying the object and deliver a suitable message if not.
	 * 
	 * @param obj
	 *            The AdvObject you want to drop
	 */
	public void executeDropCommand(AdvObject obj) {
		//If that object exists in this game and in your bag:
		if (obj != null && this.inventory.contains(obj)) {
			//Remove object from inventory, add it to current room:
			this.inventory.remove(obj);
			this.currentRoom.addObject(obj);
			System.out.println(obj.getName() + " dropped.");
			
		}
		else {
			System.out.println("Invalid object.");
		}
	}
	//After won or dead, quit game
	public void quitAfterEnd() {
		String command = scan.nextLine().trim().toUpperCase();
		command = synonyms.get(command);
		//make sure that the user's input is appropriate
		if (command != null) {
			if (!command.equals("QUIT")) {
				quitAfterEnd();
			}
			else {
				executeQuitCommand();
			}
		}
		else {
			quitAfterEnd();
		}
	}
	public void executeForcedCommand() {
		for (AdvMotionTableEntry e : this.currentRoom.getMotionTable()) {
			if (e.getKeyName()!= null && this.inventory.contains( objectList.get(e.getKeyName()) )) {
				if (e.getDestinationRoom() == 0) {
					endGame = true;
					quitAfterEnd();
				}
				else {
					AdvRoom nextRoom = this.rooms.get(e.getDestinationRoom());
					this.transport(nextRoom) ;
					break;
				}
			}
			else {
				if (e.getDestinationRoom() == 0) {
					endGame = true;
					quitAfterEnd();
				}
				else {
					AdvRoom nextRoom = this.rooms.get(e.getDestinationRoom());
					this.transport(nextRoom);
					break;
				}
			}
			
		}
		
	}

	/* Private instance variables */
	// Add your own instance variables here

	private SortedMap<Integer, AdvRoom> rooms = new TreeMap<Integer, AdvRoom>();
	private List<AdvObject> inventory = new ArrayList<AdvObject>();
	private Map<String, String> synonyms = new HashMap<String, String>();
	
	private static Map<String, AdvObject> objectList = new HashMap<String, AdvObject>();

	private AdvRoom currentRoom;
	
	private boolean endGame;

	// Use this scanner for any console input
	private static Scanner scan = new Scanner(System.in);

}