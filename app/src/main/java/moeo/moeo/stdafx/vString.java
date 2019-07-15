package com.example.egregory.moya.stdafx;
import java.io.UnsupportedEncodingException;
import java.util.Formatter;
import java.util.StringTokenizer;

public class vString
{
	public vString()
	{
	}
	
	public vString(String st)
	{
		m_buf = new StringBuffer(st);
		nLen = m_buf.length();
	}
	
	public vString(StringBuffer st)
	{
		m_buf = new StringBuffer(st);
		nLen = m_buf.length();
	}
	
	public vString(byte buf[])
	{
		Set(buf);
	}
	
	public void Set(String st)
	{
		m_buf = new StringBuffer(st);
		nLen	= m_buf.length();
	}
	
	public void Set(byte buf[])
	{
		String st;
		try 
		{
			st = new String(buf,"UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			nLen = 0;
			return;
		}
		m_buf 	= new StringBuffer(st);
		nLen	= m_buf.length();	
	}
	
	public int GetLength()
	{
		return nLen = m_buf.length();
	}
	
	public boolean IsEqual(vString st)
	{
		return m_buf.toString().equals(st.GetBuffer().toString());
	}
	
	public boolean IsEqual(String st)
	{
		return m_buf.toString().equals(st);
	}
	
	public int Find(String st)
	{
		return Find(st,0);
	}
	
	public int Find(String st, int nStart)
	{
		if ( nStart>nLen)	return -1;
		
		String tmp = new String(m_buf);
		tmp	= tmp.substring(nStart);
		
		StringTokenizer tok = new StringTokenizer(tmp,st,true);
		if (tok.hasMoreTokens()==false)	return -1;
		if (tok.countTokens()==1)	return -1;
		
		String stFirst = tok.nextToken();
		if (stFirst.length()==1)	
		if (st.indexOf(stFirst)>=0)	return 0;
		return nStart+stFirst.length();
	}
	
	public void MakeLower()
	{
		for (int i = 0; i <m_buf.length(); i++) 
		{
		   char c = m_buf.charAt(i);
		   m_buf.setCharAt(i, Character.toLowerCase(c));
		}
	}
	
	public void MakeUpper()
	{
		for (int i = 0; i <m_buf.length(); i++) 
		{
		   char c = m_buf.charAt(i);
		   m_buf.setCharAt(i, Character.toUpperCase(c));
		}
	}
	
	public vString Left(int n)
	{
		vString tmp=null;
		
		if (n<=0)	return tmp;
		tmp = new vString(m_buf.substring(0,n));
		return tmp;
	}
	
	public vString Right(int n)
	{
		vString tmp=null;
		int nMax = m_buf.length();
		
		if (n<=0)		return tmp;
		if (nMax-n<0)	return tmp;
		tmp = new vString(m_buf.substring(nMax-n,nMax));
		return tmp;
	}
	
	public StringBuffer GetBuffer()
	{
		return m_buf;
	}
	
	public String GetString()
	{
		String tmp = new String(m_buf);
		return tmp;
	}
	
	public void Format(String format, Object ... args)
	{
		Formatter formatter = new Formatter();
		m_buf= new StringBuffer( formatter.format(format, args).toString());
		formatter.close();
	}
	
	public boolean Equal(String st)
	{
		String tmp = new String(m_buf);
		return tmp.equals(st);
	}
	public boolean IsEmpty()
	{
		if (nLen>0)	return false;
		return true;
	}
	
	public vString Add(vString st)
	{
		vString tmp = new vString(m_buf);
		tmp.GetBuffer().append(st.GetBuffer());
		nLen = tmp.GetLength();
		return tmp;
	}
	
	public void Print()
	{
		System.out.printf("%s", m_buf);
	}
	
	public boolean Open(String st)
	{		
		vFile fp =new vFile();
		if (fp.Open(st, "r")==false) return false;
		
		fp.Seek(0,vFile.SEEK_END);
		int n = (int)fp.Tell();
		fp.Seek(0, vFile.SEEK_SET);
		
		byte buf[] = new byte[n+1];
		fp.Read(buf, n);
		fp.Close();
		Set(buf);
		
		return true;
	}
	
	protected StringBuffer m_buf;
	int nLen = 0;
	
	public static void main(String args[])
	{
		vString st = new vString("123 4 567");
		
		int n = st.Find(" ,",0);
		System.out.printf("%d ", n);
		n = st.Find(" ",n+1);
		System.out.printf("%d ",n);
		n = st.Find(" ",n+1);
		System.out.printf("%d ",n);
	}
}
