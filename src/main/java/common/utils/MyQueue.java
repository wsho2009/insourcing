package common.utils;

import java.util.Iterator;
import java.util.LinkedList;

//https://www.techscore.com/tech/Java/JavaSE/Thread/5-2/
public class MyQueue {
   LinkedList queue;
   public MyQueue() {
	   queue = new LinkedList();
   }
   
   synchronized public void put(Object obj) {
	   while (queue.size() >= 100) {
		   //System.out.println(obj + "を追加しようとしましたが,キューがいっぱいなので待機します。");
		   try {
			   wait();
		   } catch (InterruptedException e) {}
	   }
	   queue.addFirst(obj);
	   //printList();
	   //System.out.println(obj + "をキューに追加しました。");
	   notifyAll();
   }
   
   synchronized public Object get() {
	   while (queue.size() == 0) {
		   //System.out.println("データを取り出そうとしましたが,キューが空なので待機します。");
		   try {
			   wait();
		   } catch (InterruptedException e) {}
	   }
	   Object obj = queue.removeLast();
	   //printList();
	   //System.out.println(obj + "をキューから取り出しました。");
	   notifyAll();
	   return obj;
   }
   
   synchronized public void printList() {
	   System.out.print("[ ");
	   for (Iterator i = queue.iterator(); i.hasNext();) {
		   System.out.print(i.next() + " ");
	   }
	   System.out.print("]  ");
   }
}
