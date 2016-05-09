:-dynamic posicao/3.

tile(0,0,[ouro, monstro]).
tile(0,1,[buraco]).
tile(0,2,[]).

tile(1,0,[]).
tile(1,1,[]).
tile(1,2,[]).

tile(2,0,[]).
tile(2,1,[monstro]).
tile(2,2,[buraco]).

posicao(0, 0, leste).

% Mudar a Direção - Rotação de 90º
virar_direita :- posicao(X,Y, norte), retract(posicao(_,_,_)), assert(posicao(X, Y, leste)),!.
virar_direita :- posicao(X,Y, oeste), retract(posicao(_,_,_)), assert(posicao(X, Y, norte)),!.
virar_direita :- posicao(X,Y, sul), retract(posicao(_,_,_)), assert(posicao(X, Y, oeste)),!.
virar_direita :- posicao(X,Y, leste), retract(posicao(_,_,_)), assert(posicao(X, Y, sul)),!.