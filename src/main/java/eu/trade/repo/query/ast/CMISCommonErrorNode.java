/*
[The "BSD license"]
Copyright (c) 2005-2009 Terence Parr
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package eu.trade.repo.query.ast;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.MissingTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.UnwantedTokenException;

/** A node representing erroneous token range in token stream */
public class CMISCommonErrorNode extends CMISCommonTree {
	public IntStream input;
	public Token start;
	public Token stop;
	public RecognitionException trappedException;

	public CMISCommonErrorNode(TokenStream input, Token start, Token stop, RecognitionException e) {
		// System.out.println("start: "+start+", stop: "+stop);
		if (stop == null || stop.getTokenIndex() < start.getTokenIndex() && stop.getType() != Token.EOF) {
			// sometimes resync does not consume a token (when LT(1) is
			// in follow set. So, stop will be 1 to left to start. adjust.
			// Also handle case where start is the first token and no token
			// is consumed during recovery; LT(-1) will return null.
			stop = start;
		}
		this.input = input;
		this.start = start;
		this.stop = stop;
		this.trappedException = e;
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public int getType() {
		return Token.INVALID_TOKEN_TYPE;
	}

	@Override
	public String getText() {
		String badText;
		if (start != null) {
			int i = start.getTokenIndex();
			int j = stop.getTokenIndex();
			if (stop.getType() == Token.EOF) {
				j = input.size();
			}
			badText = ((TokenStream) input).toString(i, j);
		} else {
			// people should subclass if they alter the tree type so this
			// next one is for sure correct.
			badText = "<unknown>";
		}
		return badText;
	}

	@Override
	public String toString() {
		if (trappedException instanceof MissingTokenException) {
			return "<missing type: " + ((MissingTokenException) trappedException).getMissingType() + ">";
		} else if (trappedException instanceof UnwantedTokenException) {
			return "<extraneous: " + ((UnwantedTokenException) trappedException).getUnexpectedToken() + ", resync=" + getText() + ">";
		} else if (trappedException instanceof MismatchedTokenException) {
			return "<mismatched token: " + trappedException.token + ", resync=" + getText() + ">";
		} else if (trappedException instanceof NoViableAltException) {
			return "<unexpected: " + trappedException.token + ", resync=" + getText() + ">";
		}
		return "<error: " + getText() + ">";
	}
}
