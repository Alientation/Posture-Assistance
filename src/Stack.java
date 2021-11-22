import java.util.EmptyStackException;

//My own implementation of a stack to avoid the stackoverflow problem that comes with recursion when using floodfill. I use shorts to conserve memory since they only consist of 2 bytes, unlike an integer's 4 bytes
//I also init it with a preset size of 50,000 to avoid having to double the array and copy elements over

public class Stack {
	private short[] stack;
	private int size;
	
	public Stack() {
		this(16);
	}
	
	public Stack(int initCap) {
		stack = new short[initCap];
		size = 0;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public void clear() {
		size = 0;
	}
	
	public int pop() {
		if (size == 0) { throw new EmptyStackException(); }
		size--;
		short num = stack[size];
		return num;
	}
	
	public void push(short i) {
		if (size == stack.length - 1) {
			doubleCapacity();
		}
		stack[size] = i;
		size++;
	}
	
	public int peek() {
		if (size == 0) { throw new EmptyStackException(); }
		return stack[size-1];
	}
	
	public void doubleCapacity() {
		short[] tempStack = new short[stack.length * 2];
		for (int i = 0; i < stack.length; i++) {
			tempStack[i] = stack[i];
		}
		stack = tempStack;
	}
}
