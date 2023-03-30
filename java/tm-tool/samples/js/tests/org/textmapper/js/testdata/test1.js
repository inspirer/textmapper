"strict";

var a = function() {};

a = function b(param) {
	return 0 + param;
};

function q() {
  return 1;
}

a = a(2) + 5;
{

	b = function(){
		a += 4; q();

		if (!a) return 1;
		else if (q()) return 3;
		else return 4;
	}();
	a += b;

	(function(){ a*=2; })();
}

// 28
console.log(a);
