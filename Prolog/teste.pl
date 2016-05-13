:-dynamic posicao/3.

% Posição do Personagem
posicao(0, 11, leste).

% Células Adjacentes
adjacente(X, Y) :- posicao(PX, Y, _), PX < 11, X is PX + 1.  
adjacente(X, Y) :- posicao(PX, Y, _), PX > 0, X is PX - 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY < 11, Y is PY + 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY > 0, Y is PY - 1.  

% Mudar a Direção - Rotação de 90º
virar_direita :- posicao(X,Y, norte), retract(posicao(_,_,_)), assert(posicao(X, Y, leste)),!.
virar_direita :- posicao(X,Y, oeste), retract(posicao(_,_,_)), assert(posicao(X, Y, norte)),!.
virar_direita :- posicao(X,Y, sul), retract(posicao(_,_,_)), assert(posicao(X, Y, oeste)),!.
virar_direita :- posicao(X,Y, leste), retract(posicao(_,_,_)), assert(posicao(X, Y, sul)),!.

% Movimentar o Personagem
	% Para Cima
andar :- posicao(X,Y,P), P = norte,  Y < 11, YY is Y + 1, retract(posicao(_,_,_)), assert(posicao(X, YY, P)),!.
    % Para Baixo
andar :- posicao(X,Y,P), P = sul,  Y > 0, YY is Y - 1, retract(posicao(_,_,_)), assert(posicao(X, YY, P)),!.
    % Para Direita
andar :- posicao(X,Y,P), P = leste,  X < 11, XX is X + 1, retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),!.
    % Para Esquerda
andar :- posicao(X,Y,P), P = oeste,  X > 0, XX is X - 1, retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),!.