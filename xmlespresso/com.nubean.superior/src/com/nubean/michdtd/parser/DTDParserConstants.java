/* Generated By:JJTree&JavaCC: Do not edit this line. DTDParserConstants.java */
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

package com.nubean.michdtd.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface DTDParserConstants {

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
  int DECLEND = 13;
  /** RegularExpression Id. */
  int DSO = 14;
  /** RegularExpression Id. */
  int DSC = 15;
  /** RegularExpression Id. */
  int PC = 16;
  /** RegularExpression Id. */
  int ANY = 17;
  /** RegularExpression Id. */
  int EMPTY = 18;
  /** RegularExpression Id. */
  int REQUIRED = 19;
  /** RegularExpression Id. */
  int IMPLIED = 20;
  /** RegularExpression Id. */
  int FIXED = 21;
  /** RegularExpression Id. */
  int ID = 22;
  /** RegularExpression Id. */
  int IDREF = 23;
  /** RegularExpression Id. */
  int IDREFS = 24;
  /** RegularExpression Id. */
  int ENTITY = 25;
  /** RegularExpression Id. */
  int ENTITIES = 26;
  /** RegularExpression Id. */
  int NMTOKEN = 27;
  /** RegularExpression Id. */
  int NMTOKENS = 28;
  /** RegularExpression Id. */
  int NOTATION = 29;
  /** RegularExpression Id. */
  int CDATA = 30;
  /** RegularExpression Id. */
  int NDATA = 31;
  /** RegularExpression Id. */
  int IGNORE = 32;
  /** RegularExpression Id. */
  int INCLUDE = 33;
  /** RegularExpression Id. */
  int PUBLIC = 34;
  /** RegularExpression Id. */
  int SYSTEM = 35;
  /** RegularExpression Id. */
  int ELEMENTO = 36;
  /** RegularExpression Id. */
  int ATTLISTO = 37;
  /** RegularExpression Id. */
  int NOTATIONO = 38;
  /** RegularExpression Id. */
  int ENTITYO = 39;
  /** RegularExpression Id. */
  int Name = 40;
  /** RegularExpression Id. */
  int Hex = 41;
  /** RegularExpression Id. */
  int Hex4 = 42;
  /** RegularExpression Id. */
  int Eq = 43;
  /** RegularExpression Id. */
  int Digits = 44;
  /** RegularExpression Id. */
  int MiscName = 45;
  /** RegularExpression Id. */
  int NameChar = 46;
  /** RegularExpression Id. */
  int CombiningChar = 47;
  /** RegularExpression Id. */
  int Extender = 48;
  /** RegularExpression Id. */
  int Space = 49;
  /** RegularExpression Id. */
  int uS = 50;
  /** RegularExpression Id. */
  int Digit = 51;
  /** RegularExpression Id. */
  int Letter = 52;
  /** RegularExpression Id. */
  int BaseChar = 53;
  /** RegularExpression Id. */
  int Ideographic = 54;
  /** RegularExpression Id. */
  int PubidLiteral = 55;
  /** RegularExpression Id. */
  int DQuote = 56;
  /** RegularExpression Id. */
  int SQuote = 57;
  /** RegularExpression Id. */
  int SpecialCharsDQuote = 58;
  /** RegularExpression Id. */
  int SpecialCharsSQuote = 59;
  /** RegularExpression Id. */
  int PubidCharDQuote = 60;
  /** RegularExpression Id. */
  int PubidCharSQuote = 61;
  /** RegularExpression Id. */
  int PubidCharsDQuote = 62;
  /** RegularExpression Id. */
  int PubidCharsSQuote = 63;
  /** RegularExpression Id. */
  int Perc = 64;
  /** RegularExpression Id. */
  int SemiColon = 65;
  /** RegularExpression Id. */
  int Amp = 66;
  /** RegularExpression Id. */
  int ChRefHex = 67;
  /** RegularExpression Id. */
  int ChRefDec = 68;
  /** RegularExpression Id. */
  int Dec = 69;
  /** RegularExpression Id. */
  int DQuotedDO = 70;
  /** RegularExpression Id. */
  int DQuotedDC = 71;
  /** RegularExpression Id. */
  int DQuotedRestAttValueD = 72;
  /** RegularExpression Id. */
  int DQuotedSO = 73;
  /** RegularExpression Id. */
  int DQuotedSC = 74;
  /** RegularExpression Id. */
  int DQuotedRestAttValueS = 75;
  /** RegularExpression Id. */
  int DQuotedRestEntityValueS = 76;
  /** RegularExpression Id. */
  int DQuotedRestEntityValueD = 77;
  /** RegularExpression Id. */
  int Comment = 78;
  /** RegularExpression Id. */
  int PI = 79;
  /** RegularExpression Id. */
  int SystemLiteral = 80;
  /** RegularExpression Id. */
  int DSystemLiteral = 81;
  /** RegularExpression Id. */
  int SSystemLiteral = 82;
  /** RegularExpression Id. */
  int SSystemRestLiteral = 83;
  /** RegularExpression Id. */
  int DSystemRestLiteral = 84;
  /** RegularExpression Id. */
  int QEncoding = 85;
  /** RegularExpression Id. */
  int Encoding = 86;
  /** RegularExpression Id. */
  int LatinName = 87;

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
  int SysLiteral = 6;
  /** Lexical state. */
  int Enum = 7;
  /** Lexical state. */
  int Publicid = 8;
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
  int SysRestSLiteral = 14;
  /** Lexical state. */
  int SysRestDLiteral = 15;

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
    "\">\"",
    "\"<![\"",
    "\"]]>\"",
    "\"#PCDATA\"",
    "\"ANY\"",
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
    "\"<!ELEMENT\"",
    "\"<!ATTLIST\"",
    "\"<!NOTATION\"",
    "\"<!ENTITY\"",
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
    "<Comment>",
    "<PI>",
    "<SystemLiteral>",
    "<DSystemLiteral>",
    "<SSystemLiteral>",
    "<SSystemRestLiteral>",
    "<DSystemRestLiteral>",
    "<QEncoding>",
    "<Encoding>",
    "<LatinName>",
    "\"[\"",
    "\"(\"",
    "\"|\"",
    "\")\"",
    "\",\"",
    "\"?\"",
    "\"*\"",
    "\"+\"",
  };

}
