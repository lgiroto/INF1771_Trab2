:-dynamic posicao/3.
:-dynamic amigos_salvos/1.
:-dynamic conhecimento/4.
:-dynamic tile/3.

% Mapa
tile(0,10,monstro).
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

% Células Adjacentes
adjacente(X, Y) :- posicao(PX, Y, _), PX < 11, X is PX + 1.  
adjacente(X, Y) :- posicao(PX, Y, _), PX > 0, X is PX - 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY < 11, Y is PY + 1.  
adjacente(X, Y) :- posicao(X, PY, _), PY > 0, Y is PY - 1.

% Adicionar Conhecimento
adicionar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, _)), assert(conhecimento(X, Y, S, 0)).

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

% Salvar Amigo
salvar_amigo :- posicao(X, Y, _), tile(X, Y, amigo), amigos_salvos(A), AA is A + 1,
				retract(amigos_salvos(_)), assert(amigos_salvos(AA)),
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

   	% Atacar Inimigo em não Visitado se não tiver nenhum não visitado que não tenha nada
	%acao(X) :- X = atacar.

	% Virar-se caso tenha algum adjacente não visitado que não tenha nada ou seja amigo
acao(A) :- adjacente(X, Y), conhecimento(X, Y, T, 0), (T = nada; T = amigo), A = virar_direita,!.

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

	% Virar-se para vasculhar caso ainda tenha algum não visitado que não tenha nada ou tenha amigo
acao(A) :- conhecimento(_, _, T, 0), (T = nada; T = amigo), A = virar_direita,!.

	% Arriscar Monstro

	% Arriscar andar aonde tenha buraco caso não tenha mais não visitado que não tenha nada nem amigo
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   not(conhecimento(_, _, nada, 0)), conhecimento(X, YY, brisa, 0),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   not(conhecimento(_, _, nada, 0)), conhecimento(X, YY, brisa, 0),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   not(conhecimento(_, _, nada, 0)), conhecimento(XX, Y, brisa, 0),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   not(conhecimento(_, _, nada, 0)), conhecimento(XX, Y, brisa, 0),
		   A = andar,!.

	% Arriscar Teletransportador