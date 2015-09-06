/* Generated By:JavaCC: Do not edit this line. XML10ParserConstants.java */
/*
The MIT License (MIT)

Copyright (c) 2015 NuBean LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package com.nubean.michxml.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface XML10ParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int XMLDeclStart = 1;
  /** RegularExpression Id. */
  int PISTART = 2;
  /** RegularExpression Id. */
  int Encod = 3;
  /** RegularExpression Id. */
  int VERSION = 4;
  /** RegularExpression Id. */
  int ONEZES = 5;
  /** RegularExpression Id. */
  int ONEZED = 6;
  /** RegularExpression Id. */
  int YESZED = 7;
  /** RegularExpression Id. */
  int YESZES = 8;
  /** RegularExpression Id. */
  int NOZED = 9;
  /** RegularExpression Id. */
  int NOZES = 10;
  /** RegularExpression Id. */
  int STANDALONE = 11;
  /** RegularExpression Id. */
  int XMLDeclEnd = 12;
  /** RegularExpression Id. */
  int DSO = 13;
  /** RegularExpression Id. */
  int DSC = 14;
  /** RegularExpression Id. */
  int TAGEND = 15;
  /** RegularExpression Id. */
  int EMPTYTAGEND = 16;
  /** RegularExpression Id. */
  int MDO = 17;
  /** RegularExpression Id. */
  int PC = 18;
  /** RegularExpression Id. */
  int ANY = 19;
  /** RegularExpression Id. */
  int RMD = 20;
  /** RegularExpression Id. */
  int RMD_NONE = 21;
  /** RegularExpression Id. */
  int RMD_ALL = 22;
  /** RegularExpression Id. */
  int RMD_INTERNAL = 23;
  /** RegularExpression Id. */
  int EMPTY = 24;
  /** RegularExpression Id. */
  int REQUIRED = 25;
  /** RegularExpression Id. */
  int IMPLIED = 26;
  /** RegularExpression Id. */
  int FIXED = 27;
  /** RegularExpression Id. */
  int ID = 28;
  /** RegularExpression Id. */
  int IDREF = 29;
  /** RegularExpression Id. */
  int IDREFS = 30;
  /** RegularExpression Id. */
  int ENTITY = 31;
  /** RegularExpression Id. */
  int ENTITIES = 32;
  /** RegularExpression Id. */
  int NMTOKEN = 33;
  /** RegularExpression Id. */
  int NMTOKENS = 34;
  /** RegularExpression Id. */
  int NOTATION = 35;
  /** RegularExpression Id. */
  int CDATA = 36;
  /** RegularExpression Id. */
  int NDATA = 37;
  /** RegularExpression Id. */
  int IGNORE = 38;
  /** RegularExpression Id. */
  int INCLUDE = 39;
  /** RegularExpression Id. */
  int PUBLIC = 40;
  /** RegularExpression Id. */
  int SYSTEM = 41;
  /** RegularExpression Id. */
  int Name = 42;
  /** RegularExpression Id. */
  int Hex = 43;
  /** RegularExpression Id. */
  int Hex4 = 44;
  /** RegularExpression Id. */
  int Eq = 45;
  /** RegularExpression Id. */
  int Digits = 46;
  /** RegularExpression Id. */
  int MiscName = 47;
  /** RegularExpression Id. */
  int NameChar = 48;
  /** RegularExpression Id. */
  int CombiningChar = 49;
  /** RegularExpression Id. */
  int Extender = 50;
  /** RegularExpression Id. */
  int Space = 51;
  /** RegularExpression Id. */
  int uS = 52;
  /** RegularExpression Id. */
  int Digit = 53;
  /** RegularExpression Id. */
  int Letter = 54;
  /** RegularExpression Id. */
  int BaseChar = 55;
  /** RegularExpression Id. */
  int Ideographic = 56;
  /** RegularExpression Id. */
  int PubidLiteral = 57;
  /** RegularExpression Id. */
  int DQuote = 58;
  /** RegularExpression Id. */
  int SQuote = 59;
  /** RegularExpression Id. */
  int SpecialCharsDQuote = 60;
  /** RegularExpression Id. */
  int SpecialCharsSQuote = 61;
  /** RegularExpression Id. */
  int PubidCharDQuote = 62;
  /** RegularExpression Id. */
  int PubidCharSQuote = 63;
  /** RegularExpression Id. */
  int PubidCharsDQuote = 64;
  /** RegularExpression Id. */
  int PubidCharsSQuote = 65;
  /** RegularExpression Id. */
  int Perc = 66;
  /** RegularExpression Id. */
  int SemiColon = 67;
  /** RegularExpression Id. */
  int Amp = 68;
  /** RegularExpression Id. */
  int ChRefHex = 69;
  /** RegularExpression Id. */
  int ChRefDec = 70;
  /** RegularExpression Id. */
  int Dec = 71;
  /** RegularExpression Id. */
  int DQuotedDO = 72;
  /** RegularExpression Id. */
  int DQuotedDC = 73;
  /** RegularExpression Id. */
  int DQuotedRestAttValueD = 74;
  /** RegularExpression Id. */
  int DQuotedSO = 75;
  /** RegularExpression Id. */
  int DQuotedSC = 76;
  /** RegularExpression Id. */
  int DQuotedRestAttValueS = 77;
  /** RegularExpression Id. */
  int DQuotedRestEntityValueS = 78;
  /** RegularExpression Id. */
  int DQuotedRestEntityValueD = 79;
  /** RegularExpression Id. */
  int STAGSTART = 80;
  /** RegularExpression Id. */
  int ETAGSTART = 81;
  /** RegularExpression Id. */
  int Comment = 82;
  /** RegularExpression Id. */
  int PI = 83;
  /** RegularExpression Id. */
  int CharData = 84;
  /** RegularExpression Id. */
  int CDStart = 85;
  /** RegularExpression Id. */
  int CDataContent = 86;
  /** RegularExpression Id. */
  int CDEnd = 87;
  /** RegularExpression Id. */
  int SystemLiteral = 88;
  /** RegularExpression Id. */
  int DSystemLiteral = 89;
  /** RegularExpression Id. */
  int SSystemLiteral = 90;
  /** RegularExpression Id. */
  int SSystemRestLiteral = 91;
  /** RegularExpression Id. */
  int DSystemRestLiteral = 92;
  /** RegularExpression Id. */
  int QEncoding = 93;
  /** RegularExpression Id. */
  int Encoding = 94;
  /** RegularExpression Id. */
  int LatinName = 95;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int PCDataSect = 1;
  /** Lexical state. */
  int XMLDeclSect = 2;
  /** Lexical state. */
  int PISect = 3;
  /** Lexical state. */
  int HexSect = 4;
  /** Lexical state. */
  int EncodingSect = 5;
  /** Lexical state. */
  int Enum = 6;
  /** Lexical state. */
  int Publicid = 7;
  /** Lexical state. */
  int SysLiteral = 8;
  /** Lexical state. */
  int EntityValueSectS = 9;
  /** Lexical state. */
  int EntityValueSectD = 10;
  /** Lexical state. */
  int AttValueSectD = 11;
  /** Lexical state. */
  int AttValueSectS = 12;
  /** Lexical state. */
  int DecSect = 13;
  /** Lexical state. */
  int CDataSect = 14;
  /** Lexical state. */
  int SysRestSLiteral = 15;
  /** Lexical state. */
  int SysRestDLiteral = 16;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"<?xml\"",
    "\"<?\"",
    "\"encoding\"",
    "\"version\"",
    "\"\\\'1.0\\\'\"",
    "\"\\\"1.0\\\"\"",
    "\"\\\"yes\\\"\"",
    "\"\\\'yes\\\'\"",
    "\"\\\"no\\\"\"",
    "\"\\\'no\\\'\"",
    "\"standalone\"",
    "\"?>\"",
    "\"[\"",
    "\"]\"",
    "\">\"",
    "\"/>\"",
    "\"<!\"",
    "\"#PCDATA\"",
    "\"ANY\"",
    "\"RMD\"",
    "\"NONE\"",
    "\"ALL\"",
    "\"INTERNAL\"",
    "\"EMPTY\"",
    "\"#REQUIRED\"",
    "\"#IMPLIED\"",
    "\"#FIXED\"",
    "\"ID\"",
    "\"IDREF\"",
    "\"IDREFS\"",
    "\"ENTITY\"",
    "\"ENTITIES\"",
    "\"NMTOKEN\"",
    "\"NMTOKENS\"",
    "\"NOTATION\"",
    "\"CDATA\"",
    "\"NDATA\"",
    "\"IGNORE\"",
    "\"INCLUDE\"",
    "\"PUBLIC\"",
    "\"SYSTEM\"",
    "<Name>",
    "<Hex>",
    "<Hex4>",
    "\"=\"",
    "<Digits>",
    "<MiscName>",
    "<NameChar>",
    "<CombiningChar>",
    "<Extender>",
    "<Space>",
    "<uS>",
    "<Digit>",
    "<Letter>",
    "<BaseChar>",
    "<Ideographic>",
    "<PubidLiteral>",
    "\"\\\"\"",
    "\"\\\'\"",
    "<SpecialCharsDQuote>",
    "<SpecialCharsSQuote>",
    "<PubidCharDQuote>",
    "<PubidCharSQuote>",
    "<PubidCharsDQuote>",
    "<PubidCharsSQuote>",
    "\"%\"",
    "\";\"",
    "\"&\"",
    "\"&#x\"",
    "\"&#\"",
    "<Dec>",
    "\"\\\"\"",
    "\"\\\"\"",
    "<DQuotedRestAttValueD>",
    "\"\\\'\"",
    "\"\\\'\"",
    "<DQuotedRestAttValueS>",
    "<DQuotedRestEntityValueS>",
    "<DQuotedRestEntityValueD>",
    "\"<\"",
    "\"</\"",
    "<Comment>",
    "<PI>",
    "<CharData>",
    "\"<![CDATA[\"",
    "<CDataContent>",
    "\"]]>\"",
    "<SystemLiteral>",
    "<DSystemLiteral>",
    "<SSystemLiteral>",
    "<SSystemRestLiteral>",
    "<DSystemRestLiteral>",
    "<QEncoding>",
    "<Encoding>",
    "<LatinName>",
    "\"<!DOCTYPE\"",
    "\"(\"",
    "\"|\"",
    "\")\"",
    "\"<!ELEMENT\"",
    "\"<!ATTLIST\"",
    "\"<!NOTATION\"",
    "\"<!ENTITY\"",
    "\",\"",
    "\"?\"",
    "\"*\"",
    "\"+\"",
  };

}
