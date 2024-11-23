/*
 * Copyright © 2025 Metreeca srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metreeca.mesh.json;


import com.metreeca.mesh.tools.CodecException;

import java.io.IOException;
import java.nio.CharBuffer;

import static com.metreeca.mesh.json.JSONEvent.*;

import static java.lang.Math.max;
import static java.lang.String.format;

/**
 * JSON streaming reader.
 */
final class JSONReader {

    private final JSONCodec codec;

    private final Readable readable;

    private char last; // the last character read

    private int line;
    private int col;

    private boolean cr;
    private boolean open;

    private JSONEvent event;

    private final CharBuffer buffer=CharBuffer.allocate(1024).limit(0);
    private final StringBuilder token=new StringBuilder(100);


    JSONReader(final JSONCodec codec, final Readable readable) {
        this.codec=codec;
        this.readable=readable;
    }


    JSONEvent event() throws IOException {
        if ( event != null ) {

            return event;

        } else {

            while ( true ) {
                switch ( peek() ) {

                    case '{':

                        markup();

                        return (event=LBRACE);

                    case '}':

                        markup();

                        return (event=RBRACE);

                    case '[':

                        markup();

                        return (event=LBRACKET);

                    case ']':

                        markup();

                        return (event=RBRACKET);

                    case '"':

                        string();

                        return (event=STRING);

                    case '-':
                    case '+': // extension
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':

                        number();

                        return (event=NUMBER);

                    case 't':

                        literal("true");

                        return (event=TRUE);

                    case 'f':

                        literal("false");

                        return (event=FALSE);

                    case 'n':

                        literal("null");

                        return (event=NULL);

                    case ':':

                        markup();

                        return (event=COLON);

                    case ',':

                        markup();

                        return (event=COMMA);

                    case '\0':

                        return (event=EOF);

                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':

                        read();

                        break;

                    default:

                        return syntax("unexpected character <%s>", toString(peek()));

                }
            }

        }
    }


    String token() {

        final String value=token.toString();

        open=(event == LBRACE || event == LBRACKET);

        event=null;
        token.setLength(0);

        return value;
    }

    String token(final JSONEvent expected) throws IOException {

        if ( expected == null ) {
            throw new NullPointerException("null expected");
        }

        if ( open && expected == COMMA ) {

            open=false;

            return ",";

        } else {

            final JSONEvent actual=event();

            return actual == expected ? token() : syntax("expected %s, found %s",
                    expected.description(),
                    actual.description()
            );
        }

    }

    <T> T token(final T value) {

        token();

        return value;
    }


    <T> T semantics(final String format, final Object... args) {

        if ( format == null ) {
            throw new NullPointerException("null format");
        }

        if ( args == null ) {
            throw new NullPointerException("null args");
        }

        throw exception(format, false, args);
    }

    private <T> T syntax(final String format, final Object... args) {

        if ( format == null ) {
            throw new NullPointerException("null format");
        }

        if ( args == null ) {
            throw new NullPointerException("null args");
        }

        throw exception(format, true, args);
    }


    private CodecException exception(final String format, final boolean syntactic, final Object... args) {
        return new CodecException(format(format, args), syntactic, line+1, event == EOF ? col+1 : max(col, 1));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void markup() throws IOException {
        token.append(read());
    }


    private void string() throws IOException {

        read();

        while ( true ) {
            switch ( peek() ) {

                case '\\':

                    read();
                    escape();

                    break;

                case '\0':

                    syntax("unexpected end of input in string");

                    break;

                case '\u0001':
                case '\u0002':
                case '\u0003':
                case '\u0004':
                case '\u0005':
                case '\u0006':
                case '\u0007':
                case '\u0008':
                case '\t':
                case '\n':
                case '\u000B':
                case '\u000C':
                case '\r':
                case '\u000E':
                case '\u0010':
                case '\u0011':
                case '\u0012':
                case '\u0013':
                case '\u0014':
                case '\u0015':
                case '\u0016':
                case '\u0017':
                case '\u0018':
                case '\u0019':
                case '\u001A':
                case '\u001B':
                case '\u001C':
                case '\u001D':
                case '\u001E':
                case '\u001F':

                    syntax("unexpected control character <%s> in string", toString(peek()));

                    break;

                case '"':

                    read();

                    return;

                default:

                    token.append(read());

                    break;
            }
        }
    }

    private void escape() throws IOException {

        final char c=read();

        switch ( c ) {

            case '"':

                token.append('"');

                break;

            case '\\':

                token.append('\\');

                break;

            case '/':

                token.append('/');

                break;

            case 'b':

                token.append('\b');

                break;

            case 'f':

                token.append('\f');

                break;

            case 'r':

                token.append('\r');

                break;

            case 'n':

                token.append('\n');

                break;

            case 't':

                token.append('\t');

                break;

            case 'u':

                token.append((char)(hex() << 12|hex() << 8|hex() << 4|hex()));

                break;

            default:

                syntax("illegal escape sequence <\\%c>", c);

        }
    }

    private int hex() throws IOException {

        final char c=read();

        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')
                ? Character.digit(c, 16)
                : syntax("illegal digit in unicode escape sequence <%c>", c);
    }


    private void number() throws IOException {
        integer();
        fraction();
        exponent();
    }

    private void integer() throws IOException {

        if ( peek() == '-' || peek() == '+' ) {
            token.append(read());
        }

        if ( peek() == '0' ) {

            token.append(read());

            switch ( peek() ) {

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':

                    syntax("unexpected decimal digit after leading 0");

            }

        } else {

            digits();

        }
    }

    private void fraction() throws IOException {
        if ( peek() == '.' ) {

            token.append(read());

            digits();

        }
    }

    private void exponent() throws IOException {
        if ( peek() == 'e' || peek() == 'E' ) {

            token.append("E"); // normalize exponent case

            read();
            sign();
            digits();

        }
    }

    private void sign() throws IOException {
        if ( peek() == '-' || peek() == '+' ) {

            token.append(read());

        }
    }

    private void digits() throws IOException {

        switch ( peek() ) {

            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':

                token.append(read());

                break;

            default:

                syntax("expected decimal digit");

        }

        while ( true ) {
            switch ( peek() ) {

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':

                    token.append(read());

                    break;

                default:

                    return;

            }
        }
    }


    private void literal(final String literal) throws IOException {

        for (int i=0, n=literal.length(); i < n; ++i) {
            if ( read() != literal.charAt(i) ) {
                syntax(format("expected <%s> literal", literal));
            }
        }

        if ( peek() != '\0' && Character.isUnicodeIdentifierPart(peek()) ) {
            syntax(format("expected <%s> literal", literal));
        }

        token.append(literal);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private char peek() throws IOException {

        if ( last == '\0' ) {

            if ( !buffer.hasRemaining() ) {

                buffer.clear();

                final int n=readable.read(buffer);

                buffer.flip();

                if ( n < 0 ) {
                    return 0;
                }

            }

            last=buffer.get();

            if ( !cr && last == '\n' || (cr=(last == '\r')) ) {
                line++;
                col=0;
            } else {
                col++;
            }

        }

        return last;
    }

    private char read() throws IOException {

        final char c=peek();

        last='\0';

        return c;
    }


    private String toString(final char c) {
        return c < ' ' ? format("^%c", c+'A') : format("%c", c);
    }

}
