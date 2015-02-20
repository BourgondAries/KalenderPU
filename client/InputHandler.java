package client;

public class InputHandler
{

	public static void main(String[] args){

	java.util.Scanner sc = new java.util.Scanner(System.in);

	String[] parts = sc.nextLine().split(" ");

	for (int i = 0 ; i < parts.length ; i++) {
		System.out.println(parts[i]);
	}
	}
}
