package com.example.egregory.moya.stdafx;
import java.util.ArrayList;

public class vArray<E> extends ArrayList<E>
{
	private static final long serialVersionUID = 1L;

	public int GetSize()
	{
		return size();
	}
	
	public void Add(E e)	{	add(e);	}
	public void Add(E e,boolean bOverwrite)
	{
		if (bOverwrite && size()>0)
		{
			set(size()-1,e);
			return;
		}
		Add(e);
	}
	public void InsertAt(int n,E e)	{ add(n,e); }
	public void RemoveAt(int n)		{ remove(n);}
	public E GetAt(int n)			{ return get(n);	}
	public void RemoveAll()			{ clear(); }
	
	/*
	public static void main(String args[])
	{
		vArray<String> buf = new vArray<String>();
	
		buf.Add("dd");
		buf.Add("123");
		buf.InsertAt(0,"bbb");
	
		for (int i=0;i<buf.GetSize();i++)
		{
			System.out.printf("%s\n",buf.GetAt(i));
		}
	}	
	*/
}
