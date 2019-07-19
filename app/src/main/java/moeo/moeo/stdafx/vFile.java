package moeo.moeo.stdafx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class vFile 
{
	public vFile()
	{
	}
	
	public boolean Open (String stName,String stMode)
	{
		file = new File(stName);
		try 
		{
			fp	= new RandomAccessFile(file,stMode);
		} 
		catch (FileNotFoundException e) 
		{
			return false;
		}
		if (fp==null)	return false;
		
		dwLength = file.length();
		return true;
	}
	
	public long Tell()	{ return dwPos;}

	public void Seek(long dwOffset,int nPosition)
	{
		switch(nPosition){
		case SEEK_SET	:	dwPos = dwOffset;	break;							
		case SEEK_END	:	dwPos = dwLength-dwOffset;	break;
		case SEEK_CUR	:	dwPos = dwPos+dwOffset;		break;
		}
		try 
		{
			fp.seek(dwPos);
		} catch (IOException e) {}
	}
	
	public int Read(byte[] buf, int nRead)
	{
		int n;
		try {
			n = fp.read(buf, (int)dwPos, nRead);
		} catch (IOException e) {	return -1; }
		
		if (n>0)	dwPos+=nRead;
		return n;
	}
	
	public int Read(vString st,int nRead)
	{
		byte [] buf = new byte[nRead+1];
		int n;
		try {
			n = fp.read(buf, (int)dwPos, nRead);
		} catch (IOException e) { return -1;}
		
		if (n>0)	dwPos+=nRead;
		st.Set(buf);
		return n;
	}
	
	public boolean Write(byte[] buf,int nWrite)
	{
		try 
		{
			fp.write(buf,(int)dwPos, nWrite);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		dwPos+=nWrite;
		return true;
	}
	
	public void Close()
	{
		try {
			fp.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		fp 	 = null;
		file = null;
	}
	
	/*
	public static void main(String args[]) throws IOException
	{
		vFile fp = new vFile();
		fp.Open("/a.txt", "r");
		fp.Seek(0,SEEK_END);
		int n = (int)fp.Tell();
		
		byte [] buf = new byte[n+1];
		
		fp.Seek(0, SEEK_SET);
		fp.Read(buf, n);
		buf = null;
		
		vString st = new vString(buf);
		st.Print();
		
		fp.Seek(0, SEEK_SET);
		vString st = new vString();
		fp.Read(st, n);
		st.Print();
		
		vString st = new vString();
		st.Open("/a.txt");
		st.Print();
	}
	*/
	protected File file = null;
	protected RandomAccessFile fp = null;
	protected long dwLength = 0;
	protected long dwPos = 0;
	
	// file pointer position.
	public final static int SEEK_SET = 0;
	public final static int SEEK_END = 1;
	public final static int SEEK_CUR = 2;
}
