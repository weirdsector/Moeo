package moeo.moeo.stdafx;


public class vParser 
{
	public vParser()
	{
		bReturns = true;
		tags.pParent = null;
		tags.pParser = this;
	}
	
	public void Open(String st)
	{
		vString tmp = new vString();
		tmp.Open(st);
		Import(tmp.GetString());
	}
	
	public void Import(String st)
	{
		m_buf = new StringBuffer(st);
		Interpreting();
	}
	
	public void Interpreting()
	{
		PreInt();
	}
	
	protected void PreInt()
	{
		pRoot	= tags;
		ss.Reset();
		m_err.RemoveAll();
		tags.RemoveAll();
		
		vToken tl = new vToken(m_buf);
		tl.SetSeparator("\r\n",true);
	
		pCurrent	= pRoot;
		pCurrent.st	= null;
		
		for (int i=0;i<tl.GetSize();i++)
		{
			m_err.nLine = i+1;
			
			// remove comment.
			vString tmp = new vString(tl.GetAt(i));
			int ns = tmp.Find(";");
			if (ns>=0)
				tmp = tmp.Left(ns+1);
			
			vToken tok = new vToken(tmp);
			
			// remove tab
			tok.SetSeparator(";");
			if (tok.GetSize()==0)	continue;
			
			// is it 'tag'?
			String stGen = tok.GetAt(0);
			tok.Set(stGen);
			tok.FindNP(xfs);;
			
			int j;
			for (j=0;j<tok.GetSize();j++)
			{	
				String st = tok.GetAt(j);
				int n;
				while(st.indexOf("\t")==0)
					st = st.substring(1,st.length());
				while(st.indexOf(" ")==0)
					st = st.substring(1,st.length());
				
				if (st.length()==0)	continue;
				
				boolean bTag = false;
				if (st.length()>=2)
				{
					String st2 = st.substring(0,2);
					if (st2.equals(xfs)==true)	bTag = true;
				}
				
				if (bTag==true)
				{
					boolean bSingleTag = false;
					int neof = st.indexOf("/");
					if (neof>=0) 
					if (neof==st.length()-1)
					{
						bSingleTag = true;
						st = st.substring(0,st.length()-1);
					}
		
					String stLine = st.substring(2,st.length());
					vToken tt = new vToken(stLine);
					
					// having argument
					tt.SetSeparator(" \t");
					String tag = tt.GetAt(0);
					
					// is it inherited?	 check only case of tag:parent
					vToken ti = new vToken(tag);
					ti.SetSeparator(":");
					tag	= ti.GetAt(0);
					
					if (tag.equals("?xml")==true)		break;
					if (tag.equals("!DOCTYPE")==true)	break;
					
					String stFirst = tag.substring(0,1);
					if (stFirst.equals("/")==true)
					{
						vString tb = ss.Pop();
						tag = tag.substring(1,tag.length());
						
						if (tb==null || tb.GetString().equals(tag)==false)
						{
							// error.
						}
						
						if (pCurrent.pParent==null)
							pCurrent = null;
						else 
							pCurrent = pCurrent.pParent.Search(pCurrent.stParent);	
					}
					else
					{
						vString stParent = ss.Top();
						ss.Push(tag);
						
						//when multiple root
						if (pCurrent==null)
						{
							vTag pOldRoot = new vTag();						
							pOldRoot.Init();
							pOldRoot	= pOldRoot.Add(pRoot);
							pOldRoot.pParent	= pRoot;
							pOldRoot.stParent	= new String("/");
							
							int k;
							for (k=0;k<pOldRoot.GetCount();k++)
								((vTag)pOldRoot.Search(k)).pParent	= pOldRoot;
							
							pRoot.RemoveAll();
							pRoot.AddTail(pOldRoot);						
							pCurrent = pRoot;							
							pRoot.st = new String("/");
						}
						
						pCurrent.pParser = this;
						pCurrent = pCurrent.Add(tag);
						pCurrent.SetParent(stParent);
					}
					
					if (tt.GetSize()>1)	// argument
					{
						stLine = stLine.substring(tag.length(),stLine.length());
						
						// procedure for argument						
						vToken targs = new vToken(stLine);
						targs.SetSeparator(" \t","\"= ");
						
						// ",aa,"-->"aa"
						int m;
						for (m=0;m<targs.GetSize();m++)
						{
							StringBuffer stString = new StringBuffer();

							int m2=m;
							if ( targs.GetAt(m).equals("\"")==true)
							for (m2=m;m2<targs.GetSize();m2++)
							{
								stString.append(targs.GetAt(m2));								
								
								if (m2>m)	//<--if ( stString!="\"")
								if ( targs.GetAt(m2).equals("\"")==true)	break;
							}

							if ( stString.length()==0)	continue;

							for (int m3=0;m3<=m2-m;m3++)
							if ( targs.GetSize()>m)	// 2013/5/14 --> android ��������..
								targs.RemoveAt(m);
							targs.InsertAt(stString,m);
							stString = null;
						}
						
						for (m=0;m<targs.GetSize();m++)
						if ( targs.GetAt(m).equals("=")==true)	
							targs.RemoveAt(m--);
						else if ( targs.GetAt(m).equals(" ")==true)
							targs.RemoveAt(m--);
						else if ( targs.GetAt(m).equals("/")==true)
							targs.RemoveAt(m--);
							
						vSS	ssarg = new vSS();
						ssarg.Set(true);
					
						int na;
						vString v,a;
						for ( na=0;na<targs.GetSize();na++)
							ssarg.Push( targs.GetAt(na));
			
						v	= ssarg.Pop();						
						if (v!=null)							
						while(v.IsEmpty()==false)
						{
							a	= ssarg.Pop();							
							pCurrent.arrArg.Add( a );
							pCurrent.arrSA.Add(v);
							v	= ssarg.Pop();
							if (v==null)	break;
						}
					}
					
					if (bSingleTag==true)
					{
						vString tb2 = ss.Pop();
						if (tb2.Equal(tag)==false)						
						{
							//vString ttt;
							//ttt.Format("%s�� </�� ����������",(char*)tag);
							//exit(1);
						}

						pCurrent	= pCurrent.pParent;
					}
				}
				else
				{
					if (pCurrent!=null)
					{
						if (pCurrent.stScript==null)	pCurrent.stScript = new String();
						pCurrent.stScript = pCurrent.stScript + st + stEnter;
					}
				}
			}			
		}		
	}
	
	public String Export()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(stHeader);
		buf.append(stEnter);
		
		if ( pRoot.st==null)		return new String(buf);
		if ( pRoot.st.length()==0)	return new String(buf);

		if ( pRoot.st.equals("/")==true)
			for (int i=0;i<pRoot.GetCount();i++)
			{
				vTag pTag = (vTag) pRoot.Search(i);
				buf.append(pTag.Save());
			}
		else
			buf.append(pRoot.Save());
		return new String(buf);
	}
	
	public vTag Search(String stPath)
	{
		vToken tok = new vToken(stPath);
		tok.SetSeparator("/");

		vTag pRet = pRoot;

		// in case of multiple root
		if (pRet.st.equals("/")==true)	tok.InsertAt("/",0);

		for (int i=0;i<tok.GetSize();i++)
		{
			String tmp = tok.GetAt(i);

			if (i==0)
			{
				if (pRet.st.equals(tmp)==true)	;
				else	return null;
			}
			else
			{			
				boolean bExist = false;
				
				for (int j=0;j<pRet.GetCount();j++)
				{
					vTag pTag = (vTag)pRet.Search(j);
					String stTag = pTag.st;
					
					if (stTag.equals(tmp)==true)
					{
						pRet	= (vTag)pRet.Search(j);
						bExist	= true;
						break;
					}			
				}
				if (bExist==false)	return null;			
			}
		}
		return pRet;
	}
	
	public class vTag extends CPTRList
	{
		public vTag()
		{
			Init();
		}
		
		public vTag New(String st)
		{
			return Add(st);
		}
		
		public void Init()
		{
			RemoveAll();
			
			st 		 = null;  
			stParent = null;
			stScript = null;
			pParser  = null;
			pParent	 = null;
			
			arrArg 	= new vArray<vString>();
			arrSA	= new vArray<vString>();
		}
		
		public void Set(String stName)
		{
			if (st==null)	st = new String(stName);
		}
		
		public void SetParent(vString st)
		{
			if (st==null)	stParent = null;
			else			stParent = st.GetString();			
		}
		
		public void SetParent(String st)
		{
			stParent = st;
		}
		
		public vTag Search(String s)
		{
			return Search(s,null);
		}
		
		public vTag Search(String s,vTag pStart)
		{
			if (st.isEmpty()==true)	return null;
			if (st.equals(s)==true)
			if (pStart==null) 	return this;
			if (pStart==this)	pStart = null;
			
			for (int i=0;i<GetCount();i++)
			{
				vTag p 		= (vTag)Search(i);
				vTag pres	= p.Search(s, pStart);
				if (p==pStart)	pStart = null;
				if (pres!=null)	return pres;
			}
			return null;
		}
		
		public vTag Add(vTag pTag)
		{
			vTag pNew = Add( pTag.st);
			pNew.pParent	= pTag.pParent;
			pNew.pParser	= pTag.pParser;

			// something is neglected.
			int i;
			for (i=0;i<pTag.arrArg.GetSize();i++)
				pNew.arrArg.Add(pTag.arrArg.GetAt(i));			
			for (i=0;i<pTag.arrSA.GetSize();i++)
				pNew.arrSA.Add(pTag.arrSA.GetAt(i));
			
			pNew.AddScript( pTag.stScript);

			//sub
			for (i=0;i<pTag.GetCount();i++)
			{
				vTag pSub	= (vTag) pTag.Search(i);
				pNew.Add(pSub);
			}

			pNew.pObject	= pTag.pObject;

			return pNew;
		}
		
		public vTag Add(String s)
		{
			return Add(s,false);
		}
		
		public vTag Add(String s,boolean bOverwrite)
		{
			if (st==null)
			{
				st = new String(s);
				return this;
			}
			
			if (st.isEmpty()==true)
			{
				st = s;
				return this;
			}
			else
			{
				vTag p = null;
				if (bOverwrite==true)
				for (int i=0;i<GetCount();i++)
				{
					vTag pSub = (vTag) Search(i);
					if (pSub.st.equals(s))
					{
						p	= pSub;
						break;
					}
				}
				
				if (p==null)
				{
					p = new vTag();
					p.Init();
					AddTail(p);
				}
				
				p.pParent = this;
				p.pParser = this.pParser;
				p.Add(s);
				return p;
			}			
		}
		
		public void AddScript(String st)
		{
			if (st==null) 	return;
			
			if (stScript==null)	
			{
				stScript = new String(st);
				stScript = stScript + pParser.stEnter;
			}
			else
			stScript = stScript + st + pParser.stEnter;
		}
		
		public int GetLevel()
		{
			int n=0;
			vTag p = this;
			while(p.pParent!=null)
			{
				if ( p.pParent.st=="/")	return n;

				n++;
				p	= p.pParent;		
			}
			return n;
		}
		
		public String Save()
		{
			StringBuffer ret = new StringBuffer();
			StringBuffer tab = new StringBuffer();
			String tmp;
			
			// input return or not.
			boolean bReturn = true;
			if ( pParser.bReturns==false)
			if ( GetCount()==0)	
				bReturn = false;
			
			// count level for tab
			int i=0;
			if (pRoot.st.equals("/"))	i=1;
			for (;i<GetLevel();i++)
				
				tab.append("\t");
			
			// start field
			if ( GetLevel()>0)	tmp = String.format("%s<%s",tab,st);
			else				tmp = String.format("<%s",st);			
			ret.append(tmp);
			
			// argument
			for (i=arrArg.GetSize()-1;i>=0;i--)
			{
				String tt;
				tt = String.format(" %s=%s",arrArg.GetAt(i).GetString(),arrSA.GetAt(i).GetString());
				ret.append(tt);
			}					
			
			ret.append(">");
			if ( bReturn==true)	ret.append(pParser.stEnter);
	
			// data script
			vToken tok = new vToken(stScript);
			tok.SetSeparator("\r\n");
			if ( tab.length()!=0 )
			for (i=0;i<tok.GetSize();i++)
			{
				String temp;
				if (bReturn==true)
					temp = String.format("%s\t%s",tab,tok.GetAt(i));
				else
					temp = String.format("%s",tok.GetAt(i));

				ret.append(temp);
				if ( bReturn)	ret.append(pParser.stEnter);
			}
			
			// sub tag
			for(i=0;i<GetCount();i++)
			{
				vTag p		= (vTag)Search(i);
				ret.append(p.Save());
			}
			
			// complete field
			if ( GetLevel()>0)	
			{
				if ( bReturn)
					tmp = String.format("%s</%s>",tab,st);
				else
					tmp = String.format("</%s>",st);
			}
			else	tmp = String.format("</%s>",st);
			ret.append(tmp);
			ret.append(pParser.stEnter);
			return new String(ret);
		}
		
		public String SetArg(String st,int n)
		{
			return SetArg(st,n,true);
		}
		public String SetArg(String st,float f)
		{
			return SetArg(st,f,true);
		}
		
		public String SetArg(String st,int n,boolean bParenthesis)
		{
			String tmp = String.format("%d",n);
			return SetArg(st,tmp,bParenthesis);
		}
		
		public String SetArg(String st,float f,boolean bParenthesis)
		{
			String tmp = String.format("%f",f);
			return SetArg(st,tmp,bParenthesis);
		}
		
		public String SetArg(String st, String stValue, boolean bParenthesis)
		{
			if (st==null)	return null;
			if (st.isEmpty()==true)	return null;
					
			String stArg = st.toLowerCase();
			
			for (int i=0;i<arrArg.GetSize();i++)
			{
				vString tmp = arrArg.GetAt(i);
				tmp.MakeLower();
				if ( tmp.Equal(stValue)==true)	return tmp.GetString();
			}
			
			vString tmp = new vString(st);
			arrArg.Add(tmp);
			
			if ( bParenthesis )	stValue	= "\""+stValue+"\"";
			vString tv = new vString(stValue);
			arrSA.Add(tv);
			return stValue;
		}
		
		public String GetArg(String st)	{ return GetArg(st,true);}
		public String GetArg(String stName,boolean bRemoveParenthesis)
		{
			if (stName==null)	return null;
			if (stName.isEmpty()==true)	return null;
					
			String stArg = stName.toLowerCase();
			
			for (int i=0;i<arrArg.GetSize();i++)
			{
				vString st = arrArg.GetAt(i);
				if (st==null) return null;
				st.MakeLower();
				
				if ( st.Equal(stArg)==true)	
				{
					if ( bRemoveParenthesis)
				    {
				        vToken tok = new vToken(arrSA.GetAt(i));
						tok.Compress("\"");
						return tok.m_in;
				    }
					else return arrSA.GetAt(i).GetString();
				}
			}
						
			return "";
		}
		
		public int GetArgi(String stName)	{ return GetArgi(stName,true); }
		public int GetArgi(String stName,boolean bRemoveParenthesis)
		{
			String tmp = GetArg(stName,bRemoveParenthesis);
			return Integer.parseInt(tmp);
		}
		
		public float GetArgf(String stName)	{ return GetArgf(stName,true); }
		public float GetArgf(String stName,boolean bRemoveParenthesis)
		{
			String tmp = GetArg(stName,bRemoveParenthesis);
			return Float.parseFloat(tmp);
		}
		
		public vArray<vString> arrSA;
		public vArray<vString> arrArg;
		public String st		= null;
		public String stParent 	= null;
		public String stScript 	= null;
		public vTag pParent		= null;
		public vParser pParser	= null;
		public Object pObject 	= null;
	}
	
	public boolean bReturns = true;	
	public vTag pCurrent 	=  null;
	public vTag tags 		= new vTag();
	public vTag pRoot 		= tags;
	public StringBuffer m_buf;
	public vSS	ss 	= new vSS();
	
	class vSS
	{
		public vSS()
		{
			Reset();
		}
		
		public void Set(boolean bRepeat)	{ bRepetitive = bRepeat; }
		
		public void Close()
		{
			bRepetitive = false;
			Reset();
		}
		
		public void Reset()
		{
			ss.RemoveAll();
		}
		
		public boolean Push(vString s)
		{
			for (int i=0;i<ss.GetSize();i++)
			if (bRepetitive==false)
			if (ss.GetAt(i).IsEqual(s))
			{
				m_err.SetErr(m_err.nLine,s.GetString(),"is already defined.");
				return false;
			}
			
			ss.Add(s);
			return true;
		}
		
		public boolean Push(String s)
		{
			for (int i=0;i<ss.GetSize();i++)
			if (bRepetitive==false)
			if (ss.GetAt(i).IsEqual(s))
			{
				m_err.SetErr(m_err.nLine,s,"is already defined.");
				return false;
			}
			
			ss.Add(new vString(s));
			return true;
		}
		
		public vString Pop()
		{
			if ( ss.GetSize()<=0 )	return null;
			
			vString ret	= ss.GetAt(ss.GetSize()-1);
			ss.RemoveAt(ss.GetSize()-1);

			return ret;
		}
		
		public vString Top()
		{
			if ( ss.GetSize()<=0 )	return null;
			
			vString ret	= ss.GetAt(ss.GetSize()-1);
			return ret;
		}
		
		vArray<vString> ss = new vArray<vString>();
		
		protected boolean bRepetitive = true;
		
	}
	
	class vErr extends CPTRList
	{
		public vErr() {}
		public void Init()
		{
			nLine = 1;
			Close();
		}
		public void Close()	{ this.RemoveAll(true); }
		public void SetErr(int n,String st)
		{
			ERRMSG err = new ERRMSG();
			err.n = n;
			err.stErr = st;
			AddTail(err);
		}
		
		public void SetErr(int n,String v, String st)
		{
			ERRMSG err = new ERRMSG();
			err.n = n;
			err.stErr = v+st;
			AddTail(err);
		}
		
		public void SetErr(String st)
		{
			ERRMSG err = new ERRMSG();
			err.n = -1;
			err.stErr = st;
			AddTail(err);
		}
		
		public void SetErr(String cmd,String st)
		{
			ERRMSG err = new ERRMSG();
			err.n = -1;
			err.stErr = String.format("%s %s",cmd,st);
			AddTail(err);
		}
		
		class ERRMSG extends Object
		{
			public ERRMSG() {}
			public int n=0;
			public String stErr;
		}
		
		protected int nLine = 1;
	}

	public String stHeader	= new String("<?xml version=\"1.0\" encoding=\"euc-kr\"?>");
	public String stEnter	= new String("\r\n");
	public vErr m_err = new vErr();
	public String xfs="<>";
	
	/*
	public static void main(String args[])
	{
		vParser vp = new vParser();
		vString tmp = new vString();
		tmp.Open("x:\\a.txt");
		vp.Import(tmp.GetString());;
		
		vTag pTag = vp.Search("rxml/XML");
		
		System.out.printf("%s",pTag.GetArg("type"));
	}
	*/
}
