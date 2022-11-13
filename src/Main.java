import java.nio.file.*;
import java.io.*;

public class Main
{
    private final String[] keywords = {
            "abstract", "as", "base", "bool", "break", "byte", "case", "catch", "char", "checked", "class", "const", "continue", "decimal", "default", "delegate", "do", "double", "else", "enum", "event", "explicit",
            "extern", "false", "finally", "fixed", "float", "for", "foreach", "goto", "if", "implicit", "in", "int", "interface", "internal", "is", "lock", "long", "namespace", "new", "null", "object",
            "operator", "out", "override", "params", "private", "protected", "public", "readonly", "ref", "return", "sbyte", "sealed", "short", "sizeof", "stackalloc", "static", "string", "struct",
            "switch", "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked", "unsafe", "ushort", "using", "virtual", "void", "volatile", "while"

    };
    private final String[] operators = {
            "-", "+", "~", "!", "*", "/", "%", "++", "--", "<", ">", "<<", ">>", ">>>", "<", ">", "<=", ">=", "==", "!=", "&",
            "^", "|", "&&", "||", "?", ":", "=", "+=", "-=", "*=", "/=", "%=", "&=", "^=", "|=", "<<=", ">>=", ">>>="
    };
    private final String punctuations = ".,:;()[]{}";


    private enum Lex
    {
        None,
        String,
        Number,
        Identifier,
        Comment,
        Error,
        EOF,
    }

    private Lex lex = Lex.None;
    private String txt = "";
    private boolean decimalPoint = false;
    private boolean decimalExponent = false;

    public static void main(String[] args)
    {
        String filename = "sharpcode.txt";
        String source = "";

        try
        {
            source = Files.readString(Path.of(filename));
        }
        catch (IOException e)
        {
            System.err.println(e);
        }

        new Main().parseCode(source);
    }

    public void parseCode(String text)
    {
        int i = 0;
        while (lex != Lex.EOF)
        {
            if (i >= text.length())
            {
                if (lex == Lex.None)
                {
                    lex = Lex.EOF;
                    System.out.println("\nParsed successfully");
                }
                else
                {
                    lex = Lex.Error;
                    System.err.println("\nUnexpected end of file");
                }
                break;
            }

            char symbol = text.charAt(i);
            String operator;

            switch (lex)
            {
                case None:
                    txt = "";
                    if (Character.isWhitespace(symbol))
                    {
                    }
                    else if (punctuations.contains("" + symbol))
                    {
                        System.out.println("" + symbol + " -> Punctuation");
                    }
                    else if (symbol == '/' && text.length() > i+1 && text.charAt(i+1) == '/')
                    {
                        lex = Lex.Comment;
                        txt += "//";
                        i++;
                    }
                    else if ((operator = startsWithForTwo(text.substring(i), operators)) != null)
                    {
                        System.out.println(operator + " -> Operator");
                        i += operator.length() - 1;
                    }
                    else if ("$_".contains("" + symbol) || Character.isLetter(symbol))
                    {
                        lex = Lex.Identifier;
                        txt += symbol;
                    }
                    else if ("\"'`".contains("" + symbol))
                    {
                        lex = Lex.String;
                        txt += symbol;
                    }
                    else if (Character.isDigit(symbol))
                    {
                        lex = Lex.Number;
                        txt += symbol;
                    }
                    else
                    {
                        lex = Lex.Error;
                        txt += symbol;
                    }
                    i++;
                    break;
                case Identifier:
                    if (Character.isLetter(symbol) || Character.isDigit(symbol) || "$_".contains("" + symbol))
                    {
                        txt += symbol;
                        i++;
                    }
                    else
                    {
                        if (carry(keywords, txt))
                        {
                            System.out.println(txt + " -> Keyword");
                        }
                        else
                        {
                            System.out.println(txt + " -> Identifier");
                        }
                        lex = Lex.None;
                    }
                    break;
                case String:
                    txt += symbol;
                    if ("\"'`".contains("" + symbol))
                    {
                        System.out.println(txt + " -> String");
                        lex = Lex.None;
                    }
                    i++;
                    break;
                case Number:
                    if (Character.isDigit(symbol))
                    {
                        txt += symbol;
                        i++;
                    }
                    else if (symbol == '.')
                    {
                        if (decimalPoint)
                        {
                            lex = Lex.Error;
                        }
                        else
                        {
                            decimalPoint = true;
                            txt += symbol;
                            i++;
                        }
                    }
                    else if ("eE".contains("" + symbol))
                    {
                        if (decimalExponent)
                        {
                            lex = Lex.Error;
                        }
                        else
                        {
                            decimalExponent = true;
                            txt += symbol;
                            i++;
                        }
                    }
                    if ("+-".contains("" + symbol))
                    {
                        char lastSymbol = txt.charAt(txt.length() - 1);
                        if ("eE".contains("" + lastSymbol))
                        {
                            txt += symbol;
                            i++;
                        }
                        else if (lastSymbol == '.')
                        {
                            lex = Lex.Error;
                        }
                        else
                        {
                            lex = Lex.None;
                        }
                    }
                    else
                    {
                        try
                        {
                            Double.parseDouble(txt);
                            System.out.println(txt + " -> Number");
                            lex = Lex.None;
                        }
                        catch (NumberFormatException e)
                        {
                        lex = Lex.Error;
                        }
                    }
                case Comment:
                    if (symbol != '\n')
                    {
                        txt += symbol;
                    }
                    else
                    {
                        lex = Lex.None;
                    }
                    i++;
                    break;
                case Error:
                    System.err.println("\nInvalid token");
                    return;
                case EOF:
                    return;
            }
        }
    }

    private boolean carry(String[] haystack, String needle) {
        for (String hay: haystack) {
            if (hay.equals(needle)) return true;
        }
        return false;
    }

    private String startsWithForTwo(String text, String[] words) {
        for (String word: words) {
            if (text.startsWith(word)) return word;
        }
        return null;
    }
}