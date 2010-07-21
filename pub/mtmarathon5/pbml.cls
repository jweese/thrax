\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{pbml}[2009/12/12 (Xe)LaTeX class for PBML]
\ClassInfo{pbml}{$Id: pbml.cls 137 2010-07-02 17:12:28Z bejcek $\@gobble}

% Naming convention:
% The class redefines some standard LaTeX commands in order to comply with
% the graphical design.
% New macros have in most cases names conforming to the pattern \PBMLmacro.
% Internal macros that should not be used directly in the document conform
% to the name pattern \pbml@macro.

% Ignore size options without warning

\DeclareOption{10pt}{}
\DeclareOption{11pt}{}
\DeclareOption{12pt}{}

% Ignore one-/twoside

\DeclareOption{oneside}{}
\DeclareOption{twoside}{}

% Ignore paper sizes

\DeclareOption{a4paper}{}
\DeclareOption{a5paper}{}
\DeclareOption{b5paper}{}
\DeclareOption{letterpaper}{}
\DeclareOption{legalpaper}{}
\DeclareOption{executivepaper}{}
\DeclareOption{landscape}{}

% Switch between dummy logo and real logo

\DeclareOption{nologo}{\def\pbml@inslogo{\pbml@draftlogo}}
\DeclareOption{logo}{\def\pbml@inslogo{\pbml@real@logo}}

% Cropmarks

\DeclareOption{cropmarks}{\def\pbml@use@cropmarks{true}}

% XML with table of contents

\newif\ifpbml@xml
\DeclareOption{xml}{\pbml@xmltrue}

% Insert everything as PDF files

\newif\ifpbml@usepdf@
\DeclareOption{usepdf}{\pbml@usepdf@true}

% DejaVu fonts

\newif\ifpbml@fonts \pbml@fontstrue
\DeclareOption{nofonts}{\pbml@fontsfalse}

% Pass all global options to the "article" class

\DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}
\IfFileExists{\jobname.pagerange}{\ExecuteOptions{logo}}{\ExecuteOptions{nologo}}
\ProcessOptions

% Load the DOI, if existent
\InputIfFileExists{DOI}{
    \def\pbml@doi{doi: \thedoi{}.} % display DOI: plus the doi
    \pbml@xmltrue % turn on xml output if DOI given
  }{
    \def\pbml@doi{}  % don't display anything at the doi placeholder
  }

% Load the ISBN, if existent in the parent or this directory
\InputIfFileExists{../ISBN}{
    \def\pbml@isbn{ISBN \theisbn.} % display ISBN: plus the ISBN
  }{
    \InputIfFileExists{ISBN}{
        \def\pbml@isbn{ISBN \theisbn.} % display ISBN: plus the ISBN
      }{
        \def\pbml@isbn{}  % don't display anything at the ISBN placeholder
      }
  }

% Load the class with default options

\LoadClass[twoside]{article}

% Paper dimensions -- somewhat smaller than B5

\paperwidth 170mm
\paperheight 240mm

% Sizes (\normalsize and below kept at default)

\def\large{\@setfontsize\large\@xipt{13.5}}
\def\Large{\@setfontsize\Large\@xiipt{14.4}}
\def\LARGE{\@setfontsize\LARGE\@xivpt{18}} % unused
\def\huge{\@setfontsize\huge\@xviipt\@xviipt} % for draft logo
\let\Huge\huge % unused

% Lists

\setlength\partopsep{2\p@ \@plus 1\p@ \@minus 1\p@}
\def\@listi{\leftmargin\leftmargini
            \parsep \z@
            \topsep \z@
            \itemsep \z@}
\let\@listI\@listi
\@listi
\def\@listii {\leftmargin\leftmarginii
              \labelwidth\leftmarginii
              \advance\labelwidth-\labelsep
              \parsep \z@
              \topsep \z@
              \itemsep \z@}
\def\@listiii{\leftmargin\leftmarginiii
              \labelwidth\leftmarginiii
              \advance\labelwidth-\labelsep
              \parsep \z@
              \topsep \z@
              \itemsep \z@}
\def\@listiv {\leftmargin\leftmarginiv
              \labelwidth\leftmarginiv
              \advance\labelwidth-\labelsep}
\def\@listv  {\leftmargin\leftmarginv
              \labelwidth\leftmarginv
              \advance\labelwidth-\labelsep}
\def\@listvi {\leftmargin\leftmarginvi
              \labelwidth\leftmarginvi
              \advance\labelwidth-\labelsep}


% Skips

\bigskipamount\baselineskip
\medskipamount.5\baselineskip
\smallskipamount\medskipamount

\textfloatsep\baselineskip
\intextsep\baselineskip
\floatsep\baselineskip
\@fptop\z@
\@fpsep\baselineskip

% Page layout

\topskip\baselineskip
\parskip \z@

\ifpbml@usepdf@

\topmargin-1in
\oddsidemargin\topmargin
\evensidemargin\topmargin
\headheight\z@
\headsep\z@
\footskip\z@
\parindent\z@
\textheight\paperheight
\textwidth\paperwidth

\else

\textwidth 29cc
\evensidemargin 22mm
\oddsidemargin \paperwidth
\advance\oddsidemargin -\textwidth
\advance\oddsidemargin -\evensidemargin
\advance\oddsidemargin -1in
\advance\evensidemargin -1in
\footskip 24\p@
\headheight 12\p@
\headsep 24\p@
\textheight 42\baselineskip
\topmargin 22mm
\advance\topmargin -1in
\parindent 14\p@

\fi

% Sectioning, only \section, \subsection and \subsubsection supported

\def\section{\@startsection{section}\@ne\z@{12\p@}{12\p@ minus 6\p@}%
            {\normalfont\large\bfseries}}
\def\pbml@subsection{\@startsection{subsection}\tw@\z@{12\p@}{12\p@ minus 6\p@}%
                    {\normalfont\bfseries}}
\def\pbml@subsubsection{\@startsection{subsubsection}{3}\z@{12\p@}{12\p@ minus 6\p@}%
                       {\normalfont}}
\def\pbml@xsubsection{\@startsection{subsection}\tw@\z@{12\p@}{-.5em}%
                     {\normalfont\bfseries}}
\def\pbml@xsubsubsection{\@startsection{subsubsection}{3}\z@{12\p@}{-.5em}%
                        {\normalfont\bfseries}}

\def\subsection{\@ifstar{\pbml@subsection*}{\pbml@testsec{subsection}}}
\def\subsubsection{\@ifstar{\pbml@subsubsection*}{\pbml@testsec{subsubsection}}}

\def\pbml@testsec#1#2{\ifcat$#2$\@nameuse{pbml@x#1}{}\else\@nameuse{pbml@#1}{#2}\fi}

\setcounter{secnumdepth}{3}
\renewcommand\@seccntformat[1]{\@nameuse{the#1}.\hspace{.5em}}

% Abstract

\renewenvironment{abstract}{\normalfont\normalsize
  \vskip 12\p@ minus 4\p@\par
  \noindent\textbf{Abstract}\par \small}{\par
  \vskip 12\p@ minus 4\p@ \hrule height \p@}

% Captions, always centered

\abovecaptionskip\baselineskip
\belowcaptionskip\baselineskip

\def\@makecaption#1#2{\vskip\abovecaptionskip
  {\centering\normalfont\normalsize\sffamily\itshape #1. #2\par}\vskip\belowcaptionskip}



% Required packages
\RequirePackage{euler}      % math
\RequirePackage{graphicx}   % needed for logo
\RequirePackage{multicol}   % multicolumn text
\RequirePackage{keyval}     % author names and addresses
\RequirePackage{url}        % URL formatting
\RequirePackage{natbib}     % bibliography
\RequirePackage{pbmlbib}    % bibliography per \include, no longer needed for Xpbmlmake

% XeLaTeX and amssymb have a conflict with the following five symbols:
\let\Finv\relax
\let\Game\relax
\let\beth\relax
\let\gimel\relax
\let\daleth\relax
\RequirePackage{amssymb}

% Save the definition of \url

\let\PBMLurl\url

% Make a dummy definition so that we have at least something

\let\HyperrefURL\url

% Slanted -> italics

\let\sl\it
\let\textsl\textit

% Widows and clubs forbidden by default
\widowpenalty= 10000
\clubpenalty= 10000

% Bibliography

\renewenvironment{thebibliography}[1]{%
 \section*{\bibname}\small
 \list
 {}{\setlength{\labelwidth}{0pt}\setlength{\leftmargin}{\parindent}
 \setlength{\itemindent}{-\parindent}}
 \def\newblock{\hskip .11em plus .33em minus -.07em}
 \sloppy\clubpenalty4000\widowpenalty4000
 \sfcode`\.=1000\relax}%
 {\endlist}

% Hacked references, replacing fullname with natbib

\def\cite{\citep}
\def\shortcite{\citeyearpar}
\def\namecite{\citet}
\def\citegood{\citealp}
\def\shortcitegood{\citeyear}
\def\namecitegood{\citealt}

% Define logos

\def\pbml@real@logo{\includegraphics{PBMLlogo}}
\def\pbml@draftlogo{\leavevmode\vbox{\hsize 15mm \LARGE \sf \bfseries
     \noindent\hbox to \hsize{P\hss B\hss M\hss L}\par \vskip 2\p@
     \hrule height \p@
     \vrule height 8mm depth 7mm width \p@
     \hfill \tiny logo \hfill
     \vrule width \p@
     \hrule}}

% Cross references, prefix will be defined by \PBMLinclude

\def\pbml@prefix{} % default for single article
\let\pbml@label\label
\let\pbml@pageref\pageref
\let\pbml@ref\ref
\def\pbml@redef#1 {\expandafter
    \def\csname#1\endcsname{\begingroup \@sanitize \csname pbml@@#1\endcsname}%
    \expandafter
    \def\csname pbml@@#1\endcsname##1{\endgroup\csname pbml@#1\endcsname
                                     {\pbml@prefix##1}}}
\pbml@redef label
\pbml@redef pageref
\pbml@redef ref

% Enable all character in \label, \bibcite and triends

\let\pbml@newlabel\@newl@bel
\let\pbml@testdef\@testdef
\def\pbml@x@redef#1 {\expandafter
    \def\csname#1\endcsname{\begingroup \@sanitize \csname pbml@@#1\endcsname}%
    \expandafter
    \def\csname pbml@@#1\endcsname##1##2{\endgroup\csname pbml@#1\endcsname{##1}{##2}}}
%\pbml@x@redef @newl@bel
%\pbml@x@redef @testdef

% PBML Info

\define@key{PBMLinfo}{number}{\def\pbml@number{#1}}
\define@key{PBMLinfo}{month}{\expandafter\xdef\expandafter\pbml@month
     \expandafter{\expandafter\uppercase\expandafter{#1}}}
\define@key{PBMLinfo}{year}{\def\pbml@year{#1}}

\def\pbml@year{\number\year}
\def\pbml@month{\ifcase\month\or
    JANUARY\or
    FEBRUARY\or
    MARCH\or
    APRIL\or
    MAY\or
    JUNE\or
    JULY\or
    AUGUST\or
    SEPTEMBER\or
    OCTOBER\or
    NOVEMBER\or
    DECEMBER\fi}
\def\pbml@number{???}

\def\PBMLinfo#1{\def\pbml@info@arg{#1}\setkeys{PBMLinfo}{#1}}

% For hyperref

\newif\ifPBMLhyperref
\newif\ifPBMLhyperrefexternal
\def\PBMLhyperref{\ifPBMLhyperref
\usepackage[
pdfdisplaydoctitle,
pdfcreator=XeLaTeX\ with\ Hyperref\ package,
pdfborderstyle={/S/U/W 1},
%colorlinks,
unicode,
xetex]{hyperref}
\ifPBMLhyperrefexternal
 \else
  \let\HyperrefURL\url
  \let\url\PBMLurl
\fi
\ifpbml@usepdf@ \else
  \PBMLfixevenfalse
\fi
\fi}

% Title page of an article, start with defining keys

\newif\ifpbml@aut@corresponding
\define@key{PBMLaut}{surname}{\def\pbml@surname{#1}}
\define@key{PBMLaut}{initials}{\def\pbml@initials{#1~}}
\define@key{PBMLaut}{firstname}{\def\pbml@firstname{#1}}
\define@key{PBMLaut}{institute}{\def\pbml@aut@institute{#1}}
\define@key{PBMLaut}{corresponding}[yes]{% its invoked by either corresponding,
    \strcfstr{yes}{#1}\ifsame\pbml@aut@correspondingtrue\fi% corresponding=yes,
    \strcfstr{1}{#1}\ifsame\pbml@aut@correspondingtrue\fi} % or corresponding=1
\define@key{PBMLaut}{address}{\def\pbml@aut@address{#1}}
\define@key{PBMLaut}{email}{\def\pbml@aut@email{#1}}

%% by introducing a new institute
%%  - the counter is incremented
%%  - the author's label is asigned a value (1, 2, 3, ... for a, b, c, ...)
%%  - the institute starting with an upper index (a,b,c...) is appended to
%%    token register of institutes
\newcount\authmark\authmark=0   % counter for institutes
\def\institute#1#2{%           \institute{i:ufal}{Institute of F. & App. Ling.}
    \ifcat$\the\pbml@afflist$\else%  if this is not the first institute
        \pbml@addtoaff{\\\affilindex{#1} }\fi% add newline and index (b,c,..)
    \advance\authmark by 1
    \ifnum\authmark=2           % if there is more than one institute =>
        \def\tempindex{\upindex{a}}  % => add index 'a' at the entire begining
        \pbml@afflist\expandafter{\expandafter\tempindex\the\pbml@afflist}\fi
    \expandafter\newcount\csname #1\endcsname%  new counter "\<user's label>"
    \csname #1\endcsname=\authmark
    \pbml@addtoaff{ #2}% append the name of institute to affil. token register
}

%% by introducing a new author
%%  - the author's name is appended to author token register (it is used for
%%    "Cite as" line in the footer of the first page)
%%  - the author's name and the upper indexes of their institutes are appended
%%    to token register of authors&affiliations (it is used for title page)
%%  - the author's name and address is copyied to special variables, if this
%%    author is marked as corresponding author
\def\pbml@csurname{}\def\pbml@cinitials{}\def\pbml@cfirstname{}%
\def\pbml@caut@address{}\def\pbml@caut@email{}%
\def\author#1{%
    \pbml@articletrue\def\pbml@surname{}\def\pbml@initials{}\def\pbml@firstname{}%
    \def\pbml@aut@address{}\def\pbml@aut@email{}%
    \def\pbml@aut@institute{authmark}%
    \pbml@aut@correspondingfalse%
    \setkeys{PBMLaut}{#1}\pbml@compose@autlist%
    \ifpbml@aut@corresponding%
        \let\pbml@csurname\pbml@surname%
        \let\pbml@cinitials\pbml@initials%
        \let\pbml@cfirstname\pbml@firstname%
        \let\pbml@caut@address\pbml@aut@address%
        \let\pbml@caut@email\pbml@aut@email%
    \fi}
\newcommand\testcorrespondingauthor[2]{%
    \ifx\@empty#1
        \typeout{PROBLEM: Missing corresponding author's #2.}
        \def#1{{\bf MISSING} #2}
    \fi}
\newif\ifsame
\newcommand{\strcfstr}[2]{%
  \samefalse
  \begingroup
   \def\1{#1}\def\2{#2}%
   \ifx\1\2 \endgroup\sametrue\else\endgroup\fi}

\def\pbml@compose@autlist{%
    \ifcat$\the\pbml@autlist$\else \pbml@addtoaut{, }\pbml@addtoautaff{, }\fi
    \expandafter\pbml@addtoaut\expandafter{\pbml@firstname~}%
    \expandafter\pbml@addtoaut\expandafter{\pbml@initials}%
    \expandafter\pbml@addtoaut\expandafter{\pbml@surname}%
    \expandafter\pbml@addtoautaff\expandafter{\pbml@firstname~}%
    \expandafter\pbml@addtoautaff\expandafter{\pbml@initials}%
    \expandafter\pbml@addtoautaff\expandafter{\pbml@surname}%
    \expandafter\pbml@addtoautaff\expandafter{%
        \expandafter\segmentaffil\expandafter{\pbml@aut@institute}}
}

% macros for upper index for affiliation
\def\affilindex#1{\upindex{\expandafter\@alph\csname #1\endcsname}}
\def\upindex#1{{$^{#1}$}}
% parsing the affiliation of this kind:  institute={i:ufal,i:utkl}
\def\segmentaffil#1{\firstaffil#1,,} % segmentation of more than one affiliation
\def\firstaffil#1,{%
  \if:#1:\else%
    \ifnum\authmark>1\affilindex{#1}\fi%affilindex only if there is >1 institute
  \expandafter\firstaffil\fi}

%% Token registers for three strings
% 1) Smith, Byron
% 2) Smith^a, Byron^b
% 3) ^a Oxford, ^b Cambridge
\def\pbml@addtoaut#1{\pbml@autlist\expandafter{\the\pbml@autlist #1}}
\newtoks\pbml@autlist       % list of authors
\def\pbml@addtoautaff#1{\pbml@autafflist\expandafter{\the\pbml@autafflist #1}}
\newtoks\pbml@autafflist    % list of authors with references to affiliations
\def\pbml@addtoaff#1{\pbml@afflist\expandafter{\the\pbml@afflist #1}}
\newtoks\pbml@afflist       % list of affiliations (referenced to)

\def\titlelinebreak#1{} % by default \titlelinebreak mean nothing

\def\title#{\def\pbml@title}
\def\shorttitle#{\def\pbml@pstitle}
\def\shortauthor#{\def\pbml@psauthor}
\let\shortauthors\shortauthor % alias

% Auxilliary macro for the running head

\def\pbml@def#1 {\expandafter\ifx\csname pbml@ps#1\endcsname\relax
    \expandafter\def\expandafter\pbml@temp\expandafter{\csname pbml@#1\endcsname}%
    \expandafter\let\csname pbml@ps#1\endcsname\pbml@temp
    \fi}

\newdimen\pbml@hwidth
\newbox\pbml@hbox

% Find information in the .aux file and compose the page range

\def\pbml@makerange{%
    \expandafter\ifx\csname\pbml@prefix @@lastpage@@\endcsname\relax
      \expandafter\ifx\csname\pbml@prefix @@firstpage@@\endcsname\relax
        \edef\pbml@pagerange{??--???}%
      \else
        \edef\pbml@pagerange{\@nameuse{\pbml@prefix @@firstpage@@}--???}%
      \fi
    \else
    \expandafter\ifx\csname\pbml@prefix @@firstpage@@\endcsname\relax
      \edef\pbml@pagerange{???--???}% an error has occured
    \else
      \edef\pbml@tempa{\@nameuse{\pbml@prefix @@firstpage@@}}%
      \edef\pbml@tempb{\@nameuse{\pbml@prefix @@lastpage@@}}%
      \ifnum\pbml@tempa=\pbml@tempb
        \edef\pbml@pagerange{\pbml@tempb}%
      \else
        \edef\pbml@pagerange{\pbml@tempa--\pbml@tempb}%
      \fi
    \fi \fi}

% Print the header

\def\pbml@makeheader{\cleardoublepage \normalfont \normalsize
    \pbml@mktitle@warning
    \gdef\thesection{\@arabic\c@section}%
    \setcounter{section}{0}\setcounter{footnote}{0}%
    \setcounter{table}{0}\setcounter{figure}{0}%
    \pbml@makerange
    \thispagestyle{PBMLempty}%
    \setbox\pbml@hbox \hbox{\sf \large \bfseries
      NUMBER \pbml@number \quad \pbml@month \space \pbml@year
      \ifpbml@norange\else\quad \pbml@pagerange\fi}\pbml@hwidth \wd\pbml@hbox
    \vbox to 11mm{\vss \centering \pbml@inslogo \par}\vskip 5\p@
    \centerline{\normalsize \sf \bfseries
      The Prague Bulletin of Mathematical Linguistics}\vskip \p@
    \hrule height \z@ depth .5\p@ \vskip 3\p@
    \centerline{\copy\pbml@hbox}\vskip 16\p@
      \write\@auxout{\string\PBMLbegdef{\pbml@prefix}{\number\c@page}}}

\def\pbml@mktitle@warning{\def\maketitle{\ClassWarning
    {pbml}{\string\maketitle\space is not allowed here.\MessageBreak
           Your command was ignored and procesing continues.\MessageBreak
           This error was found}}}


\newif\ifpbml@norange

% Make a title of an article

\renewcommand*\maketitle[1][0]{\pbml@makeheader
    \testcorrespondingauthor{\pbml@cfirstname}{first name}
    \testcorrespondingauthor{\pbml@csurname}{surname}
    \testcorrespondingauthor{\pbml@caut@email}{e-mail}
    \testcorrespondingauthor{\pbml@caut@address}{address}
    {\pbml@thanks
      \def\titlelinebreak{\\} % titlelinebreak should do the linebreak here
      \centering \Large \sf {\bfseries \pbml@title\par
      }\vskip 18\p@
      \the\pbml@autafflist\par\vskip 10\p@
      {\scriptsize\the\pbml@afflist\par
       %\vskip 18\p@ Available online XX.YY.ZZZZ\par TODO
      }
    }\vskip 24\p@
    \hrule height \p@
    \vskip #1\baselineskip
    \expandafter\def\expandafter\pbml@author\expandafter{\the\pbml@autlist}
    \pbml@def title \pbml@def author
    \markright{\pbml@psauthor\hfill\pbml@pstitle\ (\pbml@pagerange)}%
    \ifpbml@xml \pbml@xmlwrite \fi
    \bibliographystyle{pbmlabbrvnat}%
    \setcitestyle{round,authoryear}%
    \ifPBMLhyperref
      \hypersetup{pdftitle=\pbml@title,pdfauthor=\the\pbml@autlist,%
                  pdfsubject={The Prague Bulletin of Mathematical Linguistics \pbml@number,
                  \pbml@month\ \pbml@year, pages \pbml@pagerange},%
                  bookmarks,bookmarksopen}%
    \fi
    \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.
      \hfill Corresponding author: \texttt{\pbml@caut@email}
      \\
      Cite as: \the\pbml@autlist. \pbml@title.
      The Prague Bulletin of Mathematical Linguistics No. \pbml@number,
      \pbml@year, pp.~\pbml@pagerange.
      \pbml@isbn{}
      % \pbml@aligndoiright % place the next item right-aligned
      \pbml@aligndoileft % if fits, keep doi here, if not, place on next line
      \pbml@doi
    }
}

% put the argument (the doi) on the same line if it fits, or the next line
% always left-aligned
% according to TeXbook naruby by Petr Olsak
\def\pbml@aligndoileft #1\par{\unskip
  \hbox{} \hskip 0pt plus 1fil \penalty0
  \hbox{#1}\hskip 0pt plus 1fill\hbox{}\par}

% put the argument (the doi) on the same line if it fits, or the next line
% always right-aligned
% from TeXbook naruby by Petr Olsak
\def\pbml@aligndoiright #1\par{\unskip
  \nobreak\hfill\penalty71\hskip2em\hbox{}\nobreak\hfill
  \hbox{#1}\par}


% XML writing
\newwrite\pbml@xmlout

\ifpbml@xml
  \AtBeginDocument{\openout\pbml@xmlout \jobname.xml
    \write\pbml@xmlout{<?xml version="1.0" encoding="UTF-8" standalone="yes"?>}
    \write\pbml@xmlout{<pbml-contents number="\pbml@number" month="\pbml@month" year="\pbml@year">}}
  \AtEndDocument{\immediate\write\pbml@xmlout{</pbml-contents>}\immediate\closeout\pbml@xmlout}
\fi

\def\pbml@xmlwrite{{\let\thepage\relax
  \def\nobreakspace##1{ }%
  \write\pbml@xmlout{<item>}%
  \xdef\pbml@tempa{\the\pbml@autlist}%
  \write\pbml@xmlout{<authors>\pbml@tempa</authors>}%
  \xdef\pbml@tempt{\pbml@title}%
  \write\pbml@xmlout{<title>\pbml@tempt</title>}%
  \write\pbml@xmlout{<page>\thepage</page>}%
  \write\pbml@xmlout{</item>}}}

% Generic macro for simpler titles

\newcommand*\pbml@generic[2][0]{\pbml@norangetrue\title{#2}\pbml@makeheader
    {\pbml@thanks
      \centering \Large \sf \bfseries \pbml@title\par}\vskip #1\baselineskip
     \vskip 72\p@ minus 9\p@
     \pbml@def title
     \markright{\hfill\pbml@pstitle\ (\pbml@pagerange)}%
     \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.}}

% Specialized titles using the generic pattern

\newcommand*\editorial[1][0]{\pbml@generic[#1]{EDITORIAL}}
\newcommand*\EditorialBoard[1][0]{\PBMLemptylast\pbml@generic[#1]{EDITORIAL BOARD}}
\newcommand*\ListOfAuthors[1][0]{\pbml@generic[#1]{LIST OF AUTHORS}}
\newcommand*\InstructionsForAuthors{\@ifstar{\pbml@xInstructionsForAuthors}{\pbml@InstructionsForAuthors}}
\newcommand*\pbml@InstructionsForAuthors[1][0]{\pbml@generic[#1]{INSTRUCTIONS FOR AUTHORS}}
\newcommand*\PBMLcontent[1][0]{\PBMLemptylast\pbml@generic[#1]{CONTENTS}}
\let\PBMLcontents\PBMLcontent

\newcommand\pbml@xInstructionsForAuthors[2][INSTRUCTIONS FOR AUTHORS]{\title{#1}\pbml@makeheader
    {\pbml@thanks
      \centering \Large \sf \bfseries \pbml@title\par}\vskip 24\p@ minus 9\p@
     \pbml@def title
     \setkeys{PBMLbook}{#2}%
     {\parindent\z@ \Large \sf \textbf{\pbml@book@title}\par
      \ifx\pbml@book@author\undefined\else
        \bigskip
        \large\pbml@book@author\par
      \fi}\vskip 24ptminus 5pt
     \markright{\hfill\pbml@pstitle\ (\pbml@pagerange)}%
     \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.}}

% 2-column layout without indent and with spaces between paragraphs

\newenvironment{PBMLtwocol}{\begin{multicols}{2}\parindent\z@\parskip\baselineskip}%
                           {\end{multicols}}

% For PBML board text written in two columns of equal with and full width below
% \PBMLboardtexts[corr]{left column}{right column}{text below}
% corr is an optional dimension (default 0mm) which will be added to the calculated width
% of the left column and subtracted from the width of the right column

\newcommand\PBMLboardtexts[4][\z@]{{\pbml@ltocwidth .5\linewidth
  \advance\pbml@ltocwidth #1
  \pbml@rtocwidth\linewidth
  \advance\pbml@rtocwidth-\pbml@ltocwidth
  \sf \parindent\z@
  \def\title##1{\vskip 3\baselineskip \textbf{##1}\par\smallskip}%
  \noindent\vtop{\hsize\pbml@ltocwidth #2}\vtop{\hsize\pbml@rtocwidth #3}\vfill #4\par}}

\def\booknotices{\title{BOOK NOTICES}\pbml@makeheader
    {\pbml@thanks
      \centering \Large \sf \bfseries \pbml@title\par}\vskip 24\p@ minus 9\p@
     \pbml@def title
     \markright{\hfill\pbml@pstitle\ (\pbml@pagerange)}%
     \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.}}

\newif\ifpbml@article
\def\PBMLnote#1{\title{NOTE}\pbml@makeheader
    {\pbml@thanks
      \centering \Large \sf \bfseries \pbml@title\par}\vskip 24\p@ minus 9\p@
     \pbml@def title
     \setkeys{PBMLbook}{#1}%
     {\parindent\z@ \Large \sf \textbf{\pbml@book@title}\par
      \ifx\pbml@book@author\undefined\else
        \bigskip
        \large\pbml@book@author\par
      \fi}\vskip 24ptminus 5pt
     \markright{\hfill\pbml@pstitle\ (\pbml@pagerange)}%
     \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.}}

\def\PBMLreview#1{\title{REVIEWS}\pbml@makeheader
    {\pbml@thanks
      \centering \Large \sf \bfseries \pbml@title\par}\vskip 24\p@ minus 9\p@
     \pbml@def title
     \setkeys{PBMLbook}{#1}%
     {\parindent\z@ \Large \sf \textbf{\pbml@book@title}\par\bigskip
      \large\pbml@book@author\par\bigskip
      \normalsize\pbml@book@published\par\bigskip
      Reviewed by \pbml@book@reviewed\par}\vskip 24ptminus 5pt
     \markright{\hfill\pbml@pstitle\ (\pbml@pagerange)}%
     \pbml@mkcpr{\textcopyright\ \pbml@year\ PBML. All rights reserved.}}

\define@key{PBMLbook}{title}{\def\pbml@book@title{#1}}
\define@key{PBMLbook}{author}{\def\pbml@book@author{#1}}
\define@key{PBMLbook}{published}{\def\pbml@book@published{#1}}
\define@key{PBMLbook}{noticed}{\def\pbml@book@noticed{#1}}
\define@key{PBMLbook}{reviewed}{\def\pbml@book@reviewed{#1}}

\newenvironment{booknotice}[1]{\setkeys{PBMLbook}{#1}\hrule height \p@
   \vskip 120pt \penalty -200 \vskip -108pt
   {\parindent\z@ \sffamily
    \large \textbf{\pbml@book@title}\par \bigskip
    \normalsize \pbml@book@author \par \bigskip
    \small \pbml@book@published \par
    \expandafter\ifx\csname pbml@book@noticed\endcsname\relax \else
      \bigskip Notice by \pbml@book@noticed \par
    \fi
    \vskip 12pt minus 3pt}\small}{\par\bigskip}

\define@key{PBMLtoc}{title}{\def\pbml@toc@title{#1}}
\define@key{PBMLtoc}{authors}{\def\pbml@toc@authors{#1}}
\define@key{PBMLtoc}{page}{\def\pbml@toc@page{#1}}

\def\PBMLtocline#1{{\setkeys{PBMLtoc}{#1}\noindent\vtop{\hsize\pbml@ltocwidth \raggedright
    \textbf{\pbml@toc@title}%
    \expandafter\ifx\csname pbml@toc@authors\endcsname\relax\else
      \\\textit{\pbml@toc@authors}%
    \fi}\vtop{\hsize\pbml@rtocwidth \raggedleft \pbml@toc@page}\par}}

\newdimen\pbml@ltocwidth
\newdimen\pbml@rtocwidth
\newenvironment{PBMLtoc}[1][1cc]{\parindent\z@ \parskip.5\baselineskip \sf
  \pbml@rtocwidth#1
  \pbml@ltocwidth\linewidth
  \advance\pbml@ltocwidth-#1}{\par}

\def\PBMLtoctext#1{{\LARGE\bfseries #1}\par}

% Macros for footnotes and copyright

\def\pbml@nofootnote{\typeout{\string\footnote\space is not allowed here.^^J%
    Use \string\thanks\space instead.}}

\let\thanks\@gobble
\def\pbml@thanks{\let\footnote\pbml@nofootnote}

\footnotesep\baselineskip

\def\pbml@mkcpr#1{\setbox\pbml@C@box=\vbox{\footnotesize\sffamily
    \noindent\vrule width \z@ height \footnotesep
    #1\par}\pbml@C@ht=\ht\pbml@C@box
    \advance\pbml@C@ht\skip\footins
    \divide\pbml@C@ht\baselineskip
    \multiply\pbml@C@ht\baselineskip
    \advance\pbml@C@ht\baselineskip
    \advance\pbml@C@ht-\footskip
    \expandafter\enlargethispage\expandafter{\expandafter-\the\pbml@C@ht}%
    \thispagestyle{PBMLtitle}%
    \advance\pbml@C@ht\footskip}

\newbox\pbml@C@box
\newdimen\pbml@C@ht

\renewcommand\@makefntext[1]{\parindent 14\p@ \noindent \hb@xt@ \parindent{\hss \@makefnmark}#1}

% Page styles, optionaly with crop marks, not implemented yet

\def\ps@PBMLempty{\ps@empty
    \def\@oddfoot{\pbml@maybe@crop\oddsidemargin\hfill}%
    \def\@evenfoot{\pbml@maybe@crop\evensidemargin\hfill}}

\def\ps@PBMLtitle{\ps@PBMLempty
    \def\@oddfoot{\pbml@maybe@crop\oddsidemargin\vbox to \z@{\vss
    \ifnum\c@footnote=\z@ \footnoterule \fi
%   \copy\pbml@C@box}}}
    \ifdim\pagegoal>460pt \footnoterule \fi
%%    \hskip-4em{\the\c@footnote . \the\ht\footins\\
%%    \the\pagegoal+\the\ht\pbml@C@box(\the\pbml@C@ht)=\the\vsize}
    \copy\pbml@C@box}}}

\def\ps@PBML{%
  \def\@evenhead{\normalfont \normalsize \sf \leftmark}%
  \def\@oddhead{\normalsize \normalsize \sf \rightmark}%
  \def\@evenfoot{\pbml@maybe@crop\evensidemargin
      \normalfont \normalsize \sf \thepage \hfill}%
  \def\@oddfoot{\pbml@maybe@crop\oddsidemargin
      \normalfont \normalsize \sf \hfill \thepage}}

\def\ps@PBMLnonumber{%
  \def\@evenhead{\normalfont \normalsize \sf \leftmark}%
  \def\@oddhead{\normalsize \normalsize \sf \rightmark}%
  \def\@oddfoot{\pbml@maybe@crop\oddsidemargin\hfill}%
  \def\@evenfoot{\pbml@maybe@crop\evensidemargin\hfill}}

\def\pbml@leftmark{PBML
    \pbml@number \hfill \pbml@month\space\pbml@year}

\AtBeginDocument{\expandafter\markboth\expandafter{\pbml@leftmark}{}}

\pagestyle{PBML}

% No crop marks as default

\let\pbml@maybe@crop\@gobble

% Macro for crop marks

\newdimen\pbml@left
\newdimen\pbml@low
\pbml@low\paperheight
\advance\pbml@low -\footskip
\advance\pbml@low -\textheight
\advance\pbml@low -\headsep
\advance\pbml@low -\headheight
\advance\pbml@low -\topmargin
\advance\pbml@low -1in

\def\pbml@cropmarks#1{\pbml@left 1in \advance\pbml@left #1
    \lower\pbml@low\hbox to \z@ {\hss \unitlength 1mm \thinlines
          \begin{picture}(0,0)
          \put(0,-5){\line(0,-1){5}}
          \put(-5,0){\line(-1,0){5}}
          \put(170,-5){\line(0,-1){5}}
          \put(175,0){\line(1,0){5}}
          \put(0,245){\line(0,1){5}}
          \put(-5,240){\line(-1,0){5}}
          \put(170,245){\line(0,1){5}}
          \put(175,240){\line(1,0){5}}
          \put(5,-6){\makebox(0,0)[lt]{\normalfont \normalsize \sf
              PBML \pbml@number, \pbml@month\ \pbml@year\quad strana \thepage}}
          \put(165,-6){\makebox(0,0)[rt]{\normalfont \normalsize \sf \today}}
          \end{picture}\hskip \pbml@left}}

\def\pbml@enable@cropmarks{\let\pbml@maybe@crop\pbml@cropmarks
    \hoffset 1cm
    \voffset \hoffset
    \advance\paperheight 2\voffset
    \advance\paperwidth 2\hoffset}

\expandafter\ifx\csname pbml@use@cropmarks\endcsname\relax
\else \pbml@enable@cropmarks \fi

% Counter for included files, I do not want to rely on chapterbib

\newcount\pbml@tempcntber

% Make the prefix

\def\pbml@makeprefix{\global\advance\pbml@tempcntber\@ne
    \edef\pbml@prefix{PBML@@\number\pbml@tempcntber}}

% Conditional inclusion

\newcommand*\PBMLonly[2][]{%
  \ifcat$#1$\includeonly{#2}\else\includeonly{#1/#2}\fi}

\newif\ifpbml@include@

% Include file, usage: \PBMLinclude[directory]{file}

\def\PBMLinclude{\@ifstar\pbml@false\pbml@true}
\def\pbml@false{\pbml@include@false\pbml@include}
\def\pbml@true{\pbml@include@true\pbml@include}
\def\pbml@include{\begingroup \catcode`\_12 \catcode`\~12
                  \pbml@xinclude}
\newcommand*\pbml@xinclude[2][]{\endgroup
  \ifcat$\pbml@prefix$
    \openout\pbml@only=\jobname.only
  \fi
  \pbml@makeprefix
  \begingroup
  \renewcommand\documentclass[2][PBML]{}%
  \renewcommand\usepackage[2][PBML]{}%
  \let\input\PBMLinput
  \renewenvironment{document}{}{\pbml@endarticle\cleardoublepage}%
  \ifcat$#1$%
    \def\pbml@fn{#2}%
    \ifpbml@include@\write\pbml@only{\string\PBMLonly{#2}}\fi
  \else
    \def\pbml@fn{#1/#2}\graphicspath{{#1/}}\def\pbml@input@path{#1/}%
    \ifpbml@include@\write\pbml@only{\string\PBMLonly[#1]{#2}}\fi
  \fi
  \include{\pbml@fn}
  \@tempcntb\c@page
  \advance\@tempcntb\m@ne
  \immediate\write\@auxout{\string\PBMLenddef{\pbml@prefix}{\number\@tempcntb}}
  \endgroup}

% Macros for user defined counters (otherwise \PBMLonly will report errors)

\let\pbml@setcounter\setcounter
\def\setcounter#1{\@ifundefined{c@#1}{\pbml@newcounter{#1}}{}\pbml@setcounter{#1}}
\let\pbml@newcounter\newcounter
\def\newcounter#1{\@ifundefined{c@#1}{\pbml@newcounter{#1}}{\pbml@setcounter{#1}{0}}}

% Modify input path so that files included from a subdir find images etc.

\def\pbml@input@path{}
\def\PBMLinput#1{\pbml@saved@input{\pbml@input@path #1}}
\let\pbml@saved@input\input

% Hacks for finding the page range, requires reprocessing (similarly as \label and \ref)

\def\PBMLbegdef#1#2{\expandafter\gdef\csname#1@@firstpage@@\endcsname{#2}}
\def\PBMLenddef#1#2{\expandafter\gdef\csname#1@@lastpage@@\endcsname{#2}}

\AtEndDocument{\write\@auxout{\string\PBMLenddef{}{\number\c@page}}\pbml@fixeven}

\newwrite\pbml@only

% For PDF inclusion

\def\pbml@pdf@include{\begingroup \catcode`\_12 \catcode`\-12
                      \pbml@pdf@xinclude}

\newcommand*\pbml@pdf@xinclude[2][]{\endgroup
  \ifcat$#1$
    \def\fn{#2}
    \immediate\write\@auxout{\string\PBMLpdf{#2}}
  \else
    \def\fn{#1/#2}
    \immediate\write\@auxout{\string\PBMLpdf[#1]{#2}}
  \fi
  \immediate\openout\pbml@only \fn.pagerange
  \immediate\write\pbml@only{\string\setcounter{page}{\number\c@page}}
  \expandafter\ifx\csname pbml@info@arg\endcsname\relax \else
    \immediate\write\pbml@only{\string\PBMLinfo{\pbml@info@arg}}
  \fi
  \ifPBMLhyperref
    \immediate\write\pbml@only{\string\PBMLhyperreftrue}
  \fi
  \ifPBMLhyperrefexternal
    \immediate\write\pbml@only{\string\PBMLhyperrefexternaltrue}
  \fi
  \immediate\closeout\pbml@only
  \ifpbml@include@
    \def\pbml@next{\pbml@include@pdf@from{\fn}}
  \else
    \let\pbml@next\relax
  \fi
  \pbml@next}

\def\pbml@include@pdf@from#1{\pbml@numpages\z@
  \PBMLbegdef{}{0}\PBMLenddef{}{0}
  {\makeatletter
  \let\select@language\@gobble \let\@writefile\@gobbletwo \let\bibstyle\@gobble
  \let\newlabel\@gobbletwo \let\bibcite\@gobbletwo
  \InputIfFileExists{#1.aux}{}{\typeout{No #1.aux}}}
  \ifnum\@@lastpage@@>\z@
    \pbml@numpages\@@lastpage@@
    \ifnum\@@firstpage@@>\z@
      \advance\pbml@numpages -\@@firstpage@@
      \advance\pbml@numpages \@ne
    \fi
  \fi
  \ifnum\pbml@numpages>\z@
    \pbml@page\@ne
    \loop
      \vbox to \textheight{\hsize\textwidth
        \centerline{\includegraphics[page=\number\pbml@page]{#1.pdf}}\vss}\clearpage
    \ifnum\pbml@page<\pbml@numpages
      \advance\pbml@page\@ne
    \repeat
    \cleardoublepage
  \fi}

\newcount\pbml@numpages
\newcount\pbml@page

% End-of-article hook

\newtoks\pbml@endtoks
\def\AtEndArticle#1{\global\pbml@endtoks\expandafter{\the\pbml@endtoks #1}}
\def\pbml@endarticle{\the\pbml@endtoks \global\pbml@endtoks{}}
\def\PBMLemptylast{\AtEndArticle{\clearpage\thispagestyle{PBMLnonumber}}}

\def\pbml@endtokens{\AtEndDocument{\pbml@fixeven
  \@tempcntb\c@page
  \advance\@tempcntb\m@ne
  \immediate\write\@auxout{\string\PBMLenddef{\pbml@prefix}{\number\@tempcntb}}}}

\def\pbml@fixeven{\ifPBMLfixeven \pbml@vmode \pbml@vmode \null \fi}

\def\pbml@vmode{\ifhmode \par \fi \ifodd\c@page \clearpage \fi}

\newif\ifPBMLfixeven

% for PDF insertion

\InputIfFileExists{\jobname.pagerange}{\pbml@usepdf@false \PBMLfixeventrue}{}

\ifpbml@usepdf@
  \let\PBMLinclude\pbml@pdf@include
  \AtBeginDocument{\immediate\write\@auxout{\string\global\string\pbml@include@true}}
  \pagestyle{PBMLempty}
\fi

% This will be in AUX but should do nothing

\newcommand*\PBMLpdf[2][]{}

% Print full address of corresponding author at the end of article
\AtEndDocument{
 \ifpbml@article
  \vspace{1cm}
  \textbf{Address for correspondence:}\\
  \pbml@cfirstname~\pbml@cinitials\pbml@csurname\\
  \texttt{\pbml@caut@email}\\
  \pbml@caut@address
 \fi
}

% For print version use grayscale images, for online use color images if available.
\graphicspath{{./grayscale/}{./color}}
\ifPBMLhyperref{\graphicspath{{./color/}{./grayscale/}}}\fi

%% ========== Fonts for both LaTeX and XeLaTeX ===============

% If we don't run under XeLaTeX, then use LaTeX syntax for loading fonts and utf8x encoding
% (Unfortunately, ifxetex package is not available in LaTeX by default, which makes it almost useless.)
\expandafter\ifx\csname XeTeXrevision\endcsname\relax
  \RequirePackage[T1]{fontenc}
  \RequirePackage[utf8x]{inputenc}
   % Don't warn if fonts are not available - LaTeX wouldn't produce the same output anyway
  \IfFileExists{tgpagella.sty}{\RequirePackage{tgpagella}}{}
  \IfFileExists{tgheros.sty}{\RequirePackage{tgheros}}{}

% Otherwise, use XeLaTeX syntax
\else
  % Font packages
  \RequirePackage{fontspec,xltxtra}

  % Fonts
  \ifpbml@fonts % Don't warn about non-available fonts if \documentclass[nofonts]{pbml} is used
    \setmainfont[Mapping=tex-text]{TeX Gyre Pagella}
    \setsansfont[Mapping=tex-text,Scale=MatchLowercase]{DejaVu Sans}
    \setmonofont[Scale=MatchLowercase]{DejaVu Sans Mono}
  \fi

  % Euler fix (see `texdoc fontspec`} 
  \AtBeginDocument{\DeclareMathAlphabet\mathbf{U}{eur}{b}{n}}

  % Paper size
  \AtBeginDocument{\special{papersize=\the\paperwidth,\the\paperheight}}

  % Driver for pgf (tikz)
  \def\pgfsysdriver{pgfsys-dvipdfm.def}
\fi



%%  =========== This must be the last two lines! ============
\InputIfFileExists{\jobname.inc}{}{}
\PBMLhyperref
\endinput
