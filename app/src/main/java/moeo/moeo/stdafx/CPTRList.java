package com.example.egregory.moya.stdafx;
import java.util.ArrayList;

public class CPTRList 
{
	class PTR_BLK
	{
		PTR_BLK	pNext;
		PTR_BLK pPrev;
		Object pData;
	}
	
	void	Init()
	{
	}
	
	void	Close()
	{	
	}
	
	void AddTail(Object o)
	{
		PTR_BLK pNew = new PTR_BLK();
		pNew.pNext = null;
		pNew.pData = o;
		
		if (pEnd!=null)
		{
			pNew.pPrev = pEnd;
			pEnd.pNext = pNew;
		}
		else pNew.pPrev = null;
		
		if (pTop==null)	pTop = pNew;
		pEnd = pNew;
		nMax++;
	}
	
	public void AddHead(Object o )
	{
		PTR_BLK pNew	= new PTR_BLK();
		pNew.pData	= o;
		pNew.pPrev	= null;

		if (pTop!=null)	
		{
			pNew.pNext	= pTop;
			pTop.pPrev	= pNew;
		}
		else	pNew.pNext	= null;

		if ( pEnd==null )	pEnd= pNew;
		pTop	= pNew;
		nMax++;
		nCurrentBlk	= -1;
	}
	
	public void InsertAfter(int n,Object o)
	{
		if (n<0)
		{
			AddTail(o);
			return;
		}
		
		if (n==GetCount()-1)
		{
			AddTail(o);
			return;
		}
		
		GetAt(n);
		nCurrentBlk = -1;
		
		PTR_BLK pNew = new PTR_BLK();
		pNew.pData 	= o;
		pNew.pPrev 	= pCur;
		pNew.pNext	= pCur.pNext;
		
		pCur.pNext.pPrev 	= pNew;
		pCur.pNext			= pNew;
		nMax++;
	}
	
	public void InsertAfter(Object o)
	{
		if (pCur==pEnd)
		{
			AddTail(o);
			return;
		}
		
		PTR_BLK pNew = new PTR_BLK();
		pNew.pData	= o;
		pNew.pPrev	= pCur;
		pNew.pNext	= pCur.pNext;
		
		pCur.pNext.pPrev	= pNew;
		pCur.pNext			= pNew;
		nMax++;
	}
	
	public void InsertBefore( int n,Object o)
	{
		if ( n<0 )	
		{
			AddTail(o);
			return;
		}
		if ( n==0 )		
		{
			AddHead(o);
			return ;
		}
		
		GetAt(n);
		nCurrentBlk	= -1;

		PTR_BLK pNew	= new PTR_BLK();
		pNew.pData		= o;
		pNew.pPrev		= pCur.pPrev;
		pNew.pNext		= pCur;

		pCur.pPrev.pNext	= pNew;
		pCur.pPrev			= pNew;
		nMax++;
	}

	public void InsertBefore(Object o)
	{
		if ( pCur==pTop )		
		{
			AddHead(o);
			return ;
		}
		
		PTR_BLK pNew	= new PTR_BLK();
		pNew.pData		= o;
		pNew.pPrev		= pCur.pPrev;
		pNew.pNext		= pCur;

		pCur.pPrev.pNext	= pNew;
		pCur.pPrev			= pNew;
		nMax++;
	}
	
	public void AddTail(String st)
	{
		String pNew = new String(st);
		AddTail((Object)pNew);
	}
	
	public void AddHead(String st)
	{
		String pNew = new String(st);
		AddHead((Object)pNew);
	}
	
	public void InsertAfter(int n,String st)
	{
		String pNew = new String(st);
		InsertAfter(n,(Object)pNew);
	}
	
	public void InsertBefore(int n,String st)
	{
		String pNew = new String(st);
		InsertBefore(n,(Object)pNew);
	}
	
	public int GetCount()	{ return nMax;}

	public Object GetAt(int n)
	{
		int nCnt = 0;
		PTR_BLK pBlk = pCur = pTop;
		
		if (n>=GetCount()) 	return null;
		nCurrentBlk = n;
		
		while(pBlk!=null)
		{
			if (n==nCnt) return pBlk.pData;
			nCnt++;
			pBlk = pCur = pBlk.pNext;
		}
		nCurrentBlk = -1;
		return null;
	}
	
	public void RemoveAt(int n)
	{
		this.RemoveAt(n,false);
	}
	
	public void RemoveAt(int n,boolean bSelfDestroy)
	{
		if (GetAt(n)==null)	return;
		
		if (pCur==pTop)
		{
			pCur = pTop.pNext;
			if (bSelfDestroy==true)
			if (pTop.pData!=null)	pTop.pData = null;
			pTop = null;
			
			pTop = pCur;
			if (pTop!=null)	pTop.pPrev = null;
		}
		else if (pCur==pEnd)
		{
			pCur = pEnd.pPrev;
			if (bSelfDestroy==true)
			if (pEnd.pData!=null)	pEnd.pData = null;
			pEnd = null;
			pEnd = pCur;
			if ( pEnd!=null)	pEnd.pNext = null;
		}
		else
		{
			pCur.pPrev.pNext = pCur.pNext;
			pCur.pNext.pPrev = pCur.pPrev;
			
			PTR_BLK pTemp = pCur.pNext;
			if (bSelfDestroy==true)
			if (pCur.pData!=null) 	pCur.pData = null;
			pCur = null;
			pCur = pTemp;
		}
		
		nCurrentBlk = -1;
		nMax--;
		if (nMax<0) 	nMax = 0;
		if (nMax==0)
			pEnd = null;
	}
	
	public Object Search(int n)
	{
		if (n<0)	return null;
		if (nCurrentBlk<0)	return GetAt(n);
		if (n>=nMax) 	return null;
		
		if ( nCurrentBlk-n==-1)
		{
			nCurrentBlk = n;
			pCur = pCur.pNext;
			return pCur.pData;
		}
		if ( nCurrentBlk-n==1)
		{
			nCurrentBlk = n;
			pCur = pCur.pPrev;
			return pCur.pData;
		}
		if ( nCurrentBlk-n==0)
		{
			nCurrentBlk = n;
			return pCur.pData;
		}
		return GetAt(n);
	}
	public void RemoveAll()	{ RemoveAll(false); }
	public void RemoveAll(boolean bSelfDestroy)
	{
		PTR_BLK pTemp;
		pCur = pTop;
		while(pCur!=null)
		{
			pTemp = pCur.pNext;
			if (bSelfDestroy==true)
			if (pCur.pData!=null)	pCur.pData = null;
			pCur 	= null;
			pCur	= pTemp;
		}
			
		pCur = null;
		pTop = null;
		pEnd = null;
		nMax = 0;
		nCurrentBlk = -1;
	}
	
	protected PTR_BLK pTop = null;
	protected PTR_BLK pEnd = null;
	protected PTR_BLK pCur = null;
	
	protected int nCurrentBlk = -1;
	protected int nMax	= 0;
	ArrayList<PTR_BLK> m_datas;
	
	/*
	public static void main(String args[])
	{
		CPTRList list = new CPTRList();
		list.AddTail("1");
		list.AddTail("2");
		list.AddTail("3");
		list.AddTail("4");
		list.RemoveAll(true);
		
		for (int i=0;i<list.GetCount();i++)
		{
			String st = (String)list.Search(i);
			System.out.printf("%s\n", st);
		}
	}
	*/
}
