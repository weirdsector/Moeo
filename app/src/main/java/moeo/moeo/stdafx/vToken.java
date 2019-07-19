package moeo.moeo.stdafx;
import java.util.StringTokenizer;

public class vToken 
{
	public vToken(String st)
	{
		Set(st);
	}
	
	public vToken(StringBuffer st)
	{
		Set(st);
	}
	
	public vToken(vString st)
	{
		Set(st.GetBuffer());
	}
	
	public vToken()
	{	
		m_in = null;
	}
	
	public void Set(String st)
	{
		if (st!=null)	m_in = new String(st);
		else			m_in = null;
	}
	
	public void Set(StringBuffer st)
	{
		if (st!=null)	m_in = new String(st);
		else			m_in = null;
	}
	
	public vArray<String> m_arg = new vArray<String>();
	
	public void SetSeparator(String sep)
	{
		SetSeparator(sep,false);
	}
	
	public void SetSeparator(String sep, String op)
	{
		SetSeparator(sep,op,false);
	}
	public void SetSeparator(String sep, String op,boolean bEmpty)
	{
		if (m_in==null)	return;
		
		m_arg.RemoveAll();
		StringTokenizer t = new StringTokenizer(m_in,sep,true);
		while(t.hasMoreTokens())
		{
			String st = t.nextToken();			
			StringTokenizer t2 = new StringTokenizer(st,op,!bEmpty);
			while(t2.hasMoreTokens())
				m_arg.Add(t2.nextToken());
		}
	}
	
	public void SetSeparator(String sep,boolean bEmpty)
	{
		if (m_in==null)	return;
		
		StringTokenizer t = new StringTokenizer(m_in,sep,bEmpty);

		m_arg.RemoveAll();
		while(t.hasMoreTokens())
			m_arg.Add(t.nextToken());
	}
	
	public void FindNP(String st)
	{
		StringTokenizer t = new StringTokenizer(m_in,"<>\" ",true);
		m_arg.RemoveAll();
		
		String stOpen,stClose;
		stOpen = st.substring(0, 1);
		stClose= st.substring(1, 2);
		
		boolean bString=false;
		boolean bBlock=false;
		
		StringBuffer buf=new StringBuffer();
		
		while(t.hasMoreTokens())
		{
			String tmp = t.nextToken();
			if (tmp.equals("\"")==true)
			{
				bString =!bString;
				buf.append(tmp);				
				continue;
			}
			
			if (tmp.equals(stOpen)==true)
			{
				if (bString==false)
				if (bBlock==false)
				{				
					bBlock = true;
					m_arg.Add(new String(buf));
					buf = new StringBuffer(); 
					continue;
				}
			}
			if (tmp.equals(stClose)==true)
			{
				if (bString==false)
				if (bBlock==true)
				{
					bBlock = false;
					String stNew = String.format("<>%s",buf );
					m_arg.Add(stNew);
					buf = new StringBuffer();
					continue;
				}
			}
			
			buf.append(tmp);			
		}
		
		if (buf.length()>0)	m_arg.Add(new String(buf));
	}
	
	public void Compress(String st)
	{
		StringTokenizer tok = new StringTokenizer(m_in,st);
		if (tok.hasMoreTokens()==false)	return;
		StringBuffer tmp = new StringBuffer();
		while(tok.hasMoreTokens())
			tmp.append(tok.nextToken());
		m_in = null;
		m_in = new String(tmp);
	}
	
	public void RemoveAt(int n)	
	{ 	
		m_arg.RemoveAt(n); 
	}
	public void InsertAt(String st,int n)	
	{
		m_arg.InsertAt(n,st);
	}
	
	public void InsertAt(StringBuffer st,int n)	
	{
		m_arg.InsertAt(n,new String(st));
	}
	
	public int GetSize()		{	return m_arg.GetSize();	}
	public void RemoveAll()		{ 	m_arg.RemoveAll(); }
	public String GetAt(int n)	{	return m_arg.GetAt(n);	}
	public int i(int n)			{	return Integer.parseInt(m_arg.GetAt(n)); }
	public float f(int n)		{	return Float.parseFloat(m_arg.GetAt(n)); }
	public String m_in;
	
	public static void main(String args[])
	{
		/*
		vToken tok = new vToken("\t<babo b=3>");
		tok.FindNP("<>");
		
		for (int i=0;i<tok.GetSize();i++)
			System.out.printf("%s\n", tok.GetAt(i));
		*/
		
		
		vToken tok = new vToken("\t\t\t 1.7 *3 +");
		tok.SetSeparator("\t ","*+");
		
		for (int i=0;i<tok.GetSize();i++)
			System.out.printf("%s\n", tok.GetAt(i));
		
	}
}
