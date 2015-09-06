/* Generated By:JavaCC: Do not edit this line. CSSParserVisitor.java Version 5.0 */
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

package com.nubean.css.parser;

public interface CSSParserVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTcss node, Object data);
  public Object visit(ASTimports node, Object data);
  public Object visit(ASTmedia node, Object data);
  public Object visit(ASTpage node, Object data);
  public Object visit(ASTruleset node, Object data);
  public Object visit(ASTselector node, Object data);
  public Object visit(ASTdeclaration node, Object data);
}
/* JavaCC - OriginalChecksum=f1b11b52689282ce064c2ad2aaa7880e (do not edit this line) */
