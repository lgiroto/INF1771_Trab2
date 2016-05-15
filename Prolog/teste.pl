:-dynamic posicao/3.
:-dynamic amigos_salvos/1.
:-dynamic conhecimento/4.
:-dynamic tile/3.
:-dynamic municao/1.

% Mapa
tile(0,10,monstro).
tile(0,0,amigo).
tile(0,8,nada).
tile(0,11,nada).
tile(1,11,nada).
tile(2,11,nada).
tile(3,11,nada).
tile(4,11,nada).
tile(5,11,nada).
tile(6,11,monstro).
tile(7,11,nada).
tile(8,11,nada).
tile(9,11,monstro).
tile(10,11,nada).
tile(11,11,amigo).

% Posição do Personagem
posicao(0, 11, leste).

% Amigos Salvos
amigos_salvos(0).

% Munição
municao(10).

% Células Adjacentes da Atual
adjacente(X, Y) :- posicao(PX, Y, _), PX < 11, X is PX + 1.  
adjacente(X, Y) :- posicao(PX, Y, _), PX > 0, X is PX - 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY < 11, Y is PY + 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY > 0, Y is PY - 1.

% Células Adjacentes Quaisquer
adjacente_q(X, Y, AX, Y) :- X < 11, AX is X + 1.  
adjacente_q(X, Y, AX, Y) :- X > 0, AX is X - 1.  
adjacente_q(X, Y, X, AY) :- Y < 11, AY is Y + 1.  
adjacente_q(X, Y, X, AY) :- Y > 0, AY is Y - 1.

% Adicionar Conhecimento
adicionar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, _)), assert(conhecimento(X, Y, S, 0)).

% Atualizar Conhecimento se era Obstáculo
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X, Y, T, 0), (T = amigo; T = brisa; T = monstro; T = teletransportador),
								   retract(conhecimento(X, Y, T, 0)), assert(conhecimento(X, Y, S, 0)).

% Mudar a Direção - Rotação de 90º
virar_direita :- posicao(X,Y, norte), retract(posicao(_,_,_)), assert(posicao(X, Y, leste)),!.
virar_direita :- posicao(X,Y, oeste), retract(posicao(_,_,_)), assert(posicao(X, Y, norte)),!.
virar_direita :- posicao(X,Y, sul), retract(posicao(_,_,_)), assert(posicao(X, Y, oeste)),!.
virar_direita :- posicao(X,Y, leste), retract(posicao(_,_,_)), assert(posicao(X, Y, sul)),!.

% Movimentar o Personagem
	% Para Cima
andar :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		 retract(posicao(_,_,_)), assert(posicao(X, YY, P)),
		 retract(conhecimento(X, YY, S, _)), assert(conhecimento(X, YY, S, 1)),!.
    % Para Baixo
andar :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1,
		 retract(posicao(_,_,_)), assert(posicao(X, YY, P)),
		 retract(conhecimento(X, YY, S, _)), assert(conhecimento(X, YY, S, 1)),!.
    % Para Direita
andar :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		 retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),
		 retract(conhecimento(XX, Y, S, _)), assert(conhecimento(XX, Y, S, 1)),!.
    % Para Esquerda
andar :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1,
	     retract(posicao(_,_,_)), assert(posicao(XX, Y, P)),
		 retract(conhecimento(XX, Y, S, _)), assert(conhecimento(XX, Y, S, 1)),!.

% Virar Para Atacar
virar_atacar :- posicao(X,Y,P), P = norte, X < 11, XX is X + 1,
				conhecimento(XX, Y, monstro, _).
virar_atacar :- posicao(X,Y,P), P = sul, X > 0, XX is X - 1,
				conhecimento(XX, Y, monstro, _).
virar_atacar :- posicao(X,Y,P), P = leste, Y < 11, YY is Y + 1,
				conhecimento(X, YY, monstro, _).
virar_atacar :- posicao(X,Y,P), P = oeste, Y > 0, YY is Y - 1,
				conhecimento(X, YY, monstro, _).

% Atacar Monstro
atacar :-   municao(M), MM is M - 1,
	   		retract(municao(_)), assert(municao(MM)),!.

% Matar Monstro
matar_monstro :- adjacente(X, Y), conhecimento(X, Y, monstro, _),
		 		 retract(conhecimento(X, Y, monstro, _)), assert(conhecimento(X, Y, nada, _)),
		   	     retract(tile(XX, Y, _)), assert(tile(XX, Y, nada)),!.
				 
% Salvar Amigo
salvar_amigo :- posicao(X, Y, _), tile(X, Y, amigo), amigos_salvos(A), AA is A + 1,
				retract(amigos_salvos(_)), assert(amigos_salvos(AA)),
		 		retract(conhecimento(X, Y, amigo, _)), assert(conhecimento(X, Y, nada, _)),
				retract(tile(X,Y,amigo)), assert(tile(X,Y,nada)).

% Ações, em ordem de preferência
	
	% Morto, se a vida chegou a zero
	% acao(dead, none1, none2) :- energy(E), E < 1.

	% Escapar - Voltar pelos já visitados, virando para a direção de um já visitado se necessário e andar
	%acao(A) :- amigos_salvos(AS), AS = 3, !.

	% Salvar o Amigo
acao(A) :- posicao(X, Y, _), tile(X, Y, amigo), A = salvar_amigo.

	% Andar para não visitado sem Obstáculo (Buraco, Monstro ou Teletransportador); Grama ou Amigo
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, T, 0), (T = nada; T = amigo),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, T, 0), (T = nada; T = amigo),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, T, 0), (T = nada; T = amigo),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, T, 0), (T = nada; T = amigo),
		   A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que não tenha nada ou tenha amigo
acao(A) :- adjacente(X, Y), conhecimento(X, Y, T, 0), (T = nada; T = amigo), A = virar_direita,!.

	% Atacar Monstro caso não tenha mais não visitado que não tenha nada
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1, municao(M), M > 0,
		   not(conhecimento(_, _, nada, 0)),
		   conhecimento(X, YY, monstro, 0), vida(V), V > 20,
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1, municao(M), M > 0,
		   not(conhecimento(_, _, nada, 0)),
		   conhecimento(X, YY, monstro, 0), vida(V), V > 20,
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1, municao(M), M > 0,
		   not(conhecimento(_, _, nada, 0)),
		   conhecimento(XX, Y, monstro, 0), vida(V), V > 20,
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1, municao(M), M > 0,
		   not(conhecimento(_, _, nada, 0)),
		   conhecimento(XX, Y, monstro, 0), vida(V), V > 20,
		   A = atacar,!.

	% Arriscar andar aonde tenha buraco caso não tenha mais não visitado que não tenha nada
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   not(conhecimento(_, _, nada, 0)), not(conhecimento(_, _, monstro, 0)), conhecimento(X, YY, brisa, 0),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   not(conhecimento(_, _, nada, 0)), not(conhecimento(_, _, monstro, 0)), conhecimento(X, YY, brisa, 0),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   not(conhecimento(_, _, nada, 0)), not(conhecimento(_, _, monstro, 0)), conhecimento(XX, Y, brisa, 0),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   not(conhecimento(_, _, nada, 0)), not(conhecimento(_, _, monstro, 0)), conhecimento(XX, Y, brisa, 0),
		   A = andar,!.

	% Virar para fugir de obstáculo
acao(A) :- posicao(X, Y, P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(_,_,TV,0), (TV = nada; TV = amigo),
		   conhecimento(X, YY, T, 0), (T = monstro; T = brisa; T = teletransportador),
		   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(_,_,TV,0), (TV = nada; TV = amigo),
		   conhecimento(X, YY, T, 0), (T = monstro; T = brisa; T = teletransportador),
    	   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(_,_,TV,0), (TV = nada; TV = amigo),
		   conhecimento(XX, Y, T, 0), (T = monstro; T = brisa; T = teletransportador),
  		   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(_,_,TV,0), (TV = nada; TV = amigo),
		   conhecimento(XX, Y, T, 0), (T = monstro; T = brisa; T = teletransportador),
		   A = virar_direita,!.

	% Arriscar Teletransportador

	% Caso ainda haja não visitado sem nada ou amigo, virar-se buscando-o
/*acao(A) :- conhecimento(_, CY, T, 0), (T = nada; T = amigo),
		   posicao(X, Y, P), P = norte, Y > 0, YY is Y - 1,
		   adjacente(AX, AY), AX\=X, AY\=YY, conhecimento(AX, AY, T, 1),
		   abs(CY - YY) > abs(CY - AY),
		   A = virar_direita,!.
acao(A) :- conhecimento(_, CY, T, 0), (T = nada; T = amigo),
		   posicao(X, Y, P), P = sul, Y < 11, YY is Y + 1,
		   adjacente(AX, AY), AX\=X, AY\=YY, conhecimento(AX, AY, T, 1),
		   abs(CY - YY) > abs(CY - AY),
		   A = virar_direita,!.
acao(A) :- conhecimento(CX, _, T, 0), (T = nada; T = amigo),
		   posicao(X, Y, P), P = leste, X < 11, XX is X + 1,
		   adjacente(AX, AY), AX\=XX, AY\=Y, conhecimento(AX, AY, T, 1),
		   abs(CX - XX) > abs(CX - AX),
		   A = virar_direita,!.
acao(A) :- conhecimento(CX, _, T, 0), (T = nada; T = amigo),
		   posicao(X, Y, P), P = oeste, X > 0, XX is X - 1,
		   adjacente(AX, AY), AX\=XX, AY\=Y, conhecimento(AX, AY, T, 1),
		   abs(CX - XX) > abs(CX - AX),
		   A = virar_direita,!.*/

	% Andar em um visitado
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, _, 1),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, _, 1),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, _, 1),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, _, 1),
		   A = andar,!.

	% Virar-se caso esteja de frente para uma parede
acao(A) :- posicao(_,Y,P), P = norte, Y = 0,
		   A = virar_direita,!.
acao(A) :- posicao(_,Y,P), P = sul, Y = 11,
    	   A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = leste, X = 11,
  		   A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = oeste, X = 0,
		   A = virar_direita,!.

	% Arriscar Teletransportador