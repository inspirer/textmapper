#!/usr/bin/perl

$lapg = "/Users/evgeny/projects/bin/lapg.sh";
$javaPath = $ENV{'JAVA_HOME'};

sub mysubst($) {
	my $s = $_[0];
	my @list = ($s);

	if( $s =~ /^([^{}]*)\{([^{}]*)\}(.*)$/ ) {
	   my @temp = map { $1.$_.$3 } split /,/, $2;
	   @list = ();
	   for( @temp ) {
	       push @list, mysubst($_);
	   }
	}
	return @list;
}

sub content($) {
	open( FILE, "< $_[0]") or die "cannot open $_[0]: $!";
	my $val = $/;
	$/ = EOI;
	$content = <FILE>;
	$/ = $val;
	close(FILE);
	return $content;
}

sub deltree($) {
	my $dir = $_[0];
	my $v;
	opendir(DIR, $dir) || die "can't opendir $dir: $!";
	my @content = grep { /^[^\.]/ } readdir(DIR);
    closedir DIR;
	for $v ( @content ) {
		if( -d "$dir/$v" ) {
			deltree("$dir/$v");
		} else {
			unlink "$dir/$v" or die "cannot remove file $dir/$v";
		}
	}
	rmdir $dir or die "cannot remove $dir";
}

deltree("tmp") if -e "tmp";
mkdir("tmp", 0777) or die "cannot create tmp: $!\n";
$counter = 0;

sub rungrammar($$$) {
	my ($lang,$syntax,$inputfile) = @_;
	my ($result, $folder);

	$folder = "tmp/".$lang.$counter;
	$counter++;
	mkdir( $folder, 0777 ) or die "cannot create: $!\n";
	open(OUT,"> $folder/syntax") or die "cannot create file: $!\n";
	print OUT $syntax;
	close( OUT );

	$prevdir = `pwd`;
	chomp $prevdir;	
	
	chdir $folder;
	system( $lapg, "syntax", "parser.$lang" ) == 0 or die "not generated";
	die "not generated2" if not -e "parser.$lang";

	unlink "errors" if -e "errors";

	if( $lang eq "java" ) {
		system( "\"$javaPath/bin/javac\" \"parser.java\" \"mlexer.java\" -d ." ) == 0 or die "not executed";
		$result = `\"$javaPath/bin/java\" -cp \".\" mypackage.parser \"../../$inputfile\"` or die "no result";

	} else {
		die "unknown lang";
	}

	open( OUT, "> output" );
	print OUT $result;
	close( OUT );

	chdir $prevdir;

	return $result;
}

%hiddenOptions = map { $_ => $_; } ("in","out");

sub test($$%) {
    my ($lang,$grammar,%options) = @_;
	die "no in=" if not defined $options{'in'};
	die "no out=" if not defined $options{'out'};
	my $textopts = "";
	for( keys %options ) {
		$textopts .= ", $_=".$options{$_} unless exists $hiddenOptions{$_};
	}
	$options{"error"} = exists $options{"err"} ? "error:" : "";
	
	print "$counter: $lang$textopts ($options{'out'})\n";

	$file = content( "cases/".$lang.".prefix")
			.content( $grammar."grammar" )
			.content( "cases/".$lang.".postfix");


	for( keys %options ) {
	    $file =~ s/%%$_%%/$options{$_}/g;
	}

	my $res = rungrammar($lang,$file,$grammar.$options{'in'});
	my $original = content($grammar.$options{'out'});

	$res =~ s/\r//g;
	$original =~ s/\r//g;

	if( $res ne $original ) {
		die "difference in $grammar.$options{'out'} and in generated";
	}
}

sub getoptions($) {
   my @opts = split( /,/, $_[0]);
   map { /^\s*(\w+)\s*=\s*([\w\.\\\/]+)\s*$/ or die; $1 => $2; } @opts;
}

open(CONFIG, "< config.ini") or die "Can't find article $ARTICLE: $!\n";
while (<CONFIG>) {
	chomp;

	if( /^\s*$/ || /^#/ ) {
		# comment
	} elsif ( /^\s*\[([\w\\\/\.]+)\]\s*$/ ) {
		$grammar = $1;
		print "\nGrammar: $grammar\n\n";
	} elsif ( /^\s*(\w+)\s*:\s*(.*)$/ ) {
	    die "no grammar, line $." if not defined $grammar;
	    $lang = $1;
	    @options = mysubst($2);
		for( @options ) {
			test( $lang, $grammar, getoptions($_));
		}

	} else {
	    die "wrong config: line $.: $_";
	}
}
close(CONFIG);
