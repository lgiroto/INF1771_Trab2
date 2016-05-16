:-dynamic posicao/3.
:-dynamic conhecimento/4.
:-dynamic tile/3.
:-dynamic municao/1.
:-dynamic vida/1.

% Posição do Personagem
posicao(0,11,leste).
conhecimento(0,11,nada,1).

% Munição e Vida
vida(100).
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
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X, Y, brisa, 0), (S=amigo;S=nada;S=monstro;S=teletransportador),
								   retract(conhecimento(X, Y, brisa, 0)), assert(conhecimento(X, Y, S, 0)).
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X, Y, teletransportador, 0), (S=amigo;S=nada;S=monstro),
								   retract(conhecimento(X, Y, teletransportador, 0)), assert(conhecimento(X, Y, S, 0)).
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X, Y, monstro, 0), (S=amigo;S=nada),
								   retract(conhecimento(X, Y, monstro, 0)), assert(conhecimento(X, Y, S, 0)).
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X, Y, nada, 0), (S=amigo),
								   retract(conhecimento(X, Y, nada, 0)), assert(conhecimento(X, Y, S, 0)).

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

% Atacar Monstro
atacar(D) :-   	municao(M), MM is M - 1, vida(V), VV is V - D,
	   			retract(municao(_)), assert(municao(MM)),
	   			retract(vida(_)), assert(vida(VV)),!.

% Matar Monstro
matar_monstro(MX,MY) :- retract(conhecimento(MX, MY, monstro, _)), assert(conhecimento(MX, MY, nada, _)),
				 		adjacente_q(MX,MY,AX,AY),
				 		adjacente_q(AX,AY,AAX,AAY), conhecimento(AAX,AAY,monstro,_),
				 		retract(conhecimento(AAX, AAY, monstro, _)), assert(conhecimento(AAX, AAY, nada, _)),				 
		   	     		retract(tile(MX, MY, _)), assert(tile(MX, MY, nada)),!.

% Teletransportar
teletransportar(TX,TY) :- retract(posicao(_,_,P)), assert(posicao(TX,TY,P)).
	 
% Salvar Amigo
salvar_amigo :- posicao(X, Y, _), tile(X, Y, amigo),
		 		retract(conhecimento(X, Y, amigo, _)), assert(conhecimento(X, Y, nada, _)),
				retract(tile(X,Y,amigo)), assert(tile(X,Y,nada)).

% Pegar Power-Up
pegar_powerup :- posicao(X,Y,_), tile(X,Y,powerup),
				 municao(M), MM is M + 5,
				 vida(V), VV is V + 50,
				 retract(municao(M)), assert(municao(MM)),
				 retract(vida(V)), assert(vida(VV)),
				 retract(tile(X,Y,powerup)), assert(tile(X,Y,nada)).
				 

% Ações, em ordem de preferência

	% Salvar o Amigo
acao(A) :- posicao(X, Y, _), tile(X, Y, amigo), A = salvar_amigo.

	% Virar-se caso esteja de frente para uma parede
acao(A) :- posicao(_,Y,P), P = norte, Y = 0,
		   A = virar_direita,!.
acao(A) :- posicao(_,Y,P), P = sul, Y = 11,
    	   A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = leste, X = 11,
  		   A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = oeste, X = 0,
		   A = virar_direita,!.

	% Andar para não visitado sem Obstáculo (Buraco, Monstro ou Teletransportador); Grama ou Amigo
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, T, 0), (T = amigo; T = nada),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, T, 0), (T = amigo; T = nada),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, T, 0), (T = amigo; T = nada),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, T, 0), (T = amigo; T = nada),
		   A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que tenha amigo
acao(A) :- adjacente(X, Y), conhecimento(X, Y, amigo, 0), A = virar_direita,!.

	% Virar-se caso tenha algum adjacente não visitado que não tenha nada 
acao(A) :- adjacente(X, Y), conhecimento(X, Y, nada, 0), A = virar_direita,!.

	% Andar para Adjacente visitado que tem Adjacente livre não visitado
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, _, 1),
		   adjacente_q(X, YY, AX, AY), conhecimento(AX, AY, T, 0), (T = amigo; T = nada),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, _, 1),
		   adjacente_q(X, YY, AX, AY), conhecimento(AX, AY, T, 0), (T = amigo; T = nada),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, _, 1),
		   adjacente_q(XX, Y, AX, AY), conhecimento(AX, AY, T, 0), (T = amigo; T = nada),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, _, 1),
		   adjacente_q(XX, Y, AX, AY), conhecimento(AX, AY, T, 0), (T = amigo; T = nada),
		   A = andar,!.

	% Virar para fugir de obstáculo se há caminho livre não explorado
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

    % Buscar caminho livre se ainda existir
acao(A) :- conhecimento(_,_,T,0), (T=amigo;T=nada),
           A = buscar_livre.

	% Virar-se para monstro na adjacência
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1, municao(M), M > 0,
		   not(conhecimento(X, YY, monstro, 0)),
		   adjacente(AX, AY), conhecimento(AX, AY, monstro, 0),
		   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1, municao(M), M > 0,
		   not(conhecimento(X, YY, monstro, 0)),
		   adjacente(AX, AY), conhecimento(AX, AY, monstro, 0),
		   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1, municao(M), M > 0,
		   not(conhecimento(XX, Y, monstro, 0)),
		   adjacente(AX, AY), conhecimento(AX, AY, monstro, 0),
		   A = virar_direita,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1, municao(M), M > 0,
		   not(conhecimento(XX, Y, monstro, 0)),
		   adjacente(AX, AY), conhecimento(AX, AY, monstro, 0),
		   A = virar_direita,!.

	% Atacar Monstro caso não tenha mais não visitado que não tenha nada
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1, municao(M), M > 0, vida(V), V > 20,
 		   not(conhecimento(_,_,amigo,0)), not(conhecimento(_,_,nada,0)),
		   conhecimento(X, YY, monstro, 0),
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1, municao(M), M > 0, vida(V), V > 20,
		   not(conhecimento(_,_,amigo,0)), not(conhecimento(_,_,nada,0)),
		   conhecimento(X, YY, monstro, 0),
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1, municao(M), M > 0, vida(V), V > 20,
		   not(conhecimento(_,_,amigo,0)), not(conhecimento(_,_,nada,0)),
		   conhecimento(XX, Y, monstro, 0),
		   A = atacar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1, municao(M), M > 0, vida(V), V > 20,
		   not(conhecimento(_,_,amigo,0)), not(conhecimento(_,_,nada,0)),
		   conhecimento(XX, Y, monstro, 0),
		   A = atacar,!.

	% Arriscar andar aonde tenha buraco caso não tenha opção
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)), not(conhecimento(_,_,teletransportador,0)),
		   conhecimento(X, YY, brisa, 0),
		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)), not(conhecimento(_,_,teletransportador,0)),
		   conhecimento(X, YY, brisa, 0),
    	   A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)), not(conhecimento(_,_,teletransportador,0)),
		   conhecimento(XX, Y, brisa, 0),
  		   A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)), not(conhecimento(_,_,teletransportador,0)),
		   conhecimento(XX, Y, brisa, 0),
		   A = andar,!.

    % Buscar por monstro se não há mais o que explorar
acao(A) :- conhecimento(X,Y,monstro,0), not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,amigo,0)),
		   adjacente_q(X,Y, AX, AY), not(conhecimento(AX,AY,_,_)),
           A = buscar_monstro.

    % Virar-se para fugir de obstáculo de buraco e teletransportador
acao(A) :- posicao(X, Y, P), P = norte, Y > 0, YY is Y - 1,
		   not(conhecimento(X, YY, _, 1)), conhecimento(_,_,monstro,0),
		   A = virar_direita,!.
acao(A) :- posicao(X, Y, P), P = sul, Y < 11, YY is Y + 1,
		   not(conhecimento(X, YY, _, 1)), conhecimento(_,_,monstro,0),
		   A = virar_direita,!.
acao(A) :- posicao(X, Y, P), P = leste, X < 11, XX is X + 1,
		   not(conhecimento(XX, Y, _, 1)), conhecimento(_,_,monstro,0),
		   A = virar_direita,!.
acao(A) :- posicao(X, Y, P), P = oeste, X > 0, XX is X - 1,
		   not(conhecimento(XX, Y, _, 1)), conhecimento(_,_,monstro,0),
		   A = virar_direita,!.

	% Arriscar Teletransportador
acao(A) :- not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)),
		   posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, teletransportador, _),
		   A = andar,!.
acao(A) :- not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)),
		   posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1,
		   conhecimento(X, YY, teletransportador, _),
		   A = andar,!.
acao(A) :- not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)),
		   posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, teletransportador, _),
		   A = andar,!.
acao(A) :- not(conhecimento(_,_,nada,0)), not(conhecimento(_,_,monstro,0)),
		   posicao(X,Y,P), P = oeste, X > 0, XX is X - 1,
		   conhecimento(XX, Y, teletransportador, _),
		   A = andar,!.

	% Buscar por Teletransportador
acao(A) :- not(conhecimento(_, _, nada, 0)), not(conhecimento(_, _, monstro, 0)),
           A = buscar_teletransporte.

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

	% Arriscar Teletransportador