:-dynamic posicao/3.

tile(1,1,[ouro, monstro]).
tile(1,2,[buraco]).
tile(1,3,[]).

tile(2,1,[]).
tile(2,2,[]).
tile(2,3,[]).

tile(3,1,[]).
tile(3,2,[monstro]).
tile(3,3,[buraco]).

posicao(1,1, norte).

virar_direita :- posicao(X,Y, norte), retract(posicao(_,_,_)), assert(posicao(X, Y, leste)),!.
virar_direita :- posicao(X,Y, oeste), retract(posicao(_,_,_)), assert(posicao(X, Y, norte)),!.
virar_direita :- posicao(X,Y, sul), retract(posicao(_,_,_)), assert(posicao(X, Y, oeste)),!.
virar_direita :- posicao(X,Y, leste), retract(posicao(_,_,_)), assert(posicao(X, Y, sul)),!.

andar :- posicao(X,Y,P), P = norte,  Y < 3, YY is Y + 1, 
         retract(posicao(_,_,_)), assert(posicao(X, YY, P)),!.
		 
andar :- posicao(X,Y,P), P = sul,  Y > 1, YY is Y - 1, 
         retract(posicao(_,_,_)), assert(posicao(X, YY, P)),!.

andar :- posicao(X,Y,P), P = leste,  X < 3, XX is X + 1, 
         retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),!.

andar :- posicao(X,Y,P), P = oeste,  X > 1, XX is X - 1, 
         retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),!.

adjacente(X, Y) :- posicao(PX, Y, _), PX < 3, X is PX + 1.  
adjacente(X, Y) :- posicao(PX, Y, _), PX > 1, X is PX - 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY < 3, Y is PY + 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY > 1, Y is PY - 1.  


%ande_para(X, Y) :- retract(posicao(_,_)), assert(posicao(X, Y)).

executa_acao(X) :- posicao(PX, _, oeste), PX > 1, X = andar .
executa_acao(X) :- posicao(PX, _, leste), PX < 3, X = andar .
executa_acao(X) :- X = correr.
executa_acao(X) :- X = atacar.
executa_acao(X) :- X = observar.
executa_acao(X) :- X = pegar_item.
executa_acao(X) :- X = fugir.

