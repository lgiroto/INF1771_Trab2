:-dynamic posicao/3.
:-dynamic conhecimento/4.
:-dynamic tile/3.
:-dynamic observado/3.
:-dynamic municao/1.
:-dynamic vida/1.
:-dynamic amigos_salvos/1.

% Posição do Personagem
posicao(0,11,leste).
conhecimento(0,11,nada,1).

% Informações do Jogo
vida(100).
municao(5).
amigos_salvos(0).

% Células Adjacentes
adjacente(X, Y, AX, Y) :-  X < 11, AX is X + 1.   
adjacente(X, Y, AX, Y) :-  X > 0,  AX is X - 1.   
adjacente(X, Y, X, AY) :- Y < 11, AY is Y + 1. 
adjacente(X, Y, X, AY) :- Y > 0,  AY is Y - 1.

% Observar Célula
observar(X,Y,O) :- tile(X, Y, S), (S = nada; S = powerup), O = nada,!.
observar(X,Y,O) :- tile(X, Y, S), S = amigo, O = pedido_ajuda,!.
observar(X,Y,O) :- tile(X, Y, S), S = buraco, O = brisa,!.
observar(X,Y,O) :- tile(X, Y, S), S = teletransporte, O = som_asas,!.
observar(X,Y,O) :- tile(X, Y, S), S = monstro, O = passos,!.

% Verificar/Marcar se já observou na Célula
verificar_obs :- posicao(X,Y,_), not(observado(X,Y,_)), assert(observado(X,Y,1)).  

% Adicionar/Atualizar conhecimento
	% Se ainda não havia sido observado, registra
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, _)), assert(conhecimento(X, Y, S, 0)),!.
	% Se foi observado que não tem nada, atualiza como nada
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), S = nada,
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, S, 0)),!.
    % Se foi observado pedido_ajuda
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = pedido_ajuda,
 								   (T = brisa; T = som_asas; T = passos; T = [brisa, som_asas]; T = [som_asas, brisa];
 								   	T = [brisa, passos]; T = [passos, brisa]; T = [som_asas, passos]; T = [passos, som_asas]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, nada, 0)),!.
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = pedido_ajuda,
 								   (T = [brisa, pedido_ajuda]; T = [pedido_ajuda, brisa]; T = [som_asas, pedido_ajuda]; T = [pedido_ajuda, som_asas];
 								   	T = [passos, pedido_ajuda]; T = [pedido_ajuda, passos]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, S, 0)),!.
    % Se foi observado som_asas
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = som_asas,
 								   (T = pedido_ajuda; T = brisa; T = passos; T = [brisa, pedido_ajuda]; T = [pedido_ajuda, brisa];
 								   	T = [brisa, passos]; T = [passos, brisa]; T = [pedido_ajuda, passos]; T = [passos, pedido_ajuda]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, nada, 0)),!.
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = som_asas,
 								   (T = [brisa, som_asas]; T = [som_asas, brisa]; T = [som_asas, pedido_ajuda]; T = [pedido_ajuda, som_asas];
 								   	T = [passos, som_asas]; T = [som_asas, passos]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, S, 0)),!.
    % Se foi observado brisa
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = brisa,
 								   (T = pedido_ajuda; T = som_asas; T = passos; T = [som_asas, pedido_ajuda]; T = [pedido_ajuda, som_asas];
 								   	T = [som_asas, passos]; T = [passos, som_asas]; T = [pedido_ajuda, passos]; T = [passos, pedido_ajuda]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, nada, 0)),!. 
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = brisa,
 								   (T = [brisa, som_asas]; T = [som_asas, brisa]; T = [brisa, pedido_ajuda]; T = [pedido_ajuda, brisa];
 								   	T = [passos, brisa]; T = [brisa, passos]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, S, 0)),!.
    % Se foi observado passos
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = passos,
 								   (T = pedido_ajuda; T = som_asas; T = brisa; T = [som_asas, pedido_ajuda]; T = [pedido_ajuda, som_asas];
 								   	T = [brisa, pedido_ajuda]; T = [pedido_ajuda, brisa]; T = [brisa, som_asas]; T = [som_asas, brisa]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, nada, 0)),!. 
atualizar_conhecimento(X, Y, S) :- not(conhecimento(X, Y, _, 1)), conhecimento(X,Y,T,0), S = passos,
 								   (T = [passos, som_asas]; T = [som_asas, passos]; T = [passos, pedido_ajuda]; T = [pedido_ajuda, passos];
 								   	T = [passos, brisa]; T = [brisa, passos]),
								   retract(conhecimento(X, Y, _, 0)), assert(conhecimento(X, Y, S, 0)),!.

% Buscar pedido de ajuda do conhecimento
buscar_amigo(X, Y) :- posicao(PX,PY,_), conhecimento(X,Y,pedido_ajuda,0), (PX\=X;PY\=Y).

% Buscar caminho livre do conhecimento
buscar_livre(X, Y) :- posicao(PX,PY,_), conhecimento(X,Y,nada,0), (PX\=X;PY\=Y).

% Buscar monstro do conhecimento
buscar_monstro(X, Y) :- posicao(PX,PY,_), municao(M), M > 0, conhecimento(X,Y,som_asas,_), (PX\=X;PY\=Y),!.
buscar_monstro(X, Y) :- posicao(PX,PY,_), municao(M), M > 0, conhecimento(X,Y,passos,0), (PX\=X;PY\=Y),!.
buscar_monstro(X, Y) :- posicao(PX,PY,_), conhecimento(X,Y,passos,_), (PX\=X;PY\=Y),
						adjacente(X,Y,AX,AY), not(conhecimento(AX,AY,brisa,_)),
						(conhecimento(AX,AY,_,0); not(conhecimento(AX,AY,_,_))),!.

% Buscar powerup do conhecimento
buscar_powerup(X, Y) :- posicao(PX,PY,_), conhecimento(X,Y,powerup,_), (PX\=X;PY\=Y).

% Buscar teletransporte do conhecimento
buscar_teletransporte(X, Y) :- posicao(PX,PY,_), conhecimento(X,Y,som_asas,0), (PX\=X;PY\=Y).

% Teletransportar
teletransportar(TX,TY) :- retract(posicao(_,_,P)), assert(posicao(TX,TY,P)).

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

% Atacar
atacar :- municao(M), MM is M - 1, retract(municao(_)), assert(municao(MM)),!.

% Ouvir Grito
ouve_grito(OVX,OVY) :-  retract(conhecimento(OVX, OVY, _, _)), assert(conhecimento(OVX, OVY, nada, _)),
		   	     		retract(tile(OVX, OVY, _)), assert(tile(OVX, OVY, nada)),!.
atualiza_adj  :-  posicao(PX, PY, _), adjacente(PX, PY, AX, AY),
		 		  retract(conhecimento(AX, AY, M, _)), assert(conhecimento(AX, AY, nada, _)),
				  retract(conhecimento(AX, AY, [M,pedido_ajuda], _)), assert(conhecimento(AX, AY, pedido_ajuda, _)),	
		 		  retract(conhecimento(AX, AY, [pedido_ajuda, M], _)), assert(conhecimento(AX, AY, pedido_ajuda, _)),	
		 		  retract(conhecimento(AX, AY, [brisa, M], _)), assert(conhecimento(AX, AY, brisa, _)),	
		 		  retract(conhecimento(AX, AY, [brisa, M], _)), assert(conhecimento(AX, AY, brisa, _)),!.

% Salvar Amigo
salvar_amigo :- posicao(X, Y, _), tile(X, Y, amigo), amigos_salvos(A), AA is A + 1,
		 		retract(conhecimento(X, Y, _, _)), assert(conhecimento(X, Y, nada, _)),
				retract(tile(X,Y,amigo)), assert(tile(X,Y,nada)),
				retract(amigos_salvos(A)), assert(amigos_salvos(AA)).

% Pegar powerup
pegar_powerup :- posicao(X, Y, _), tile(X, Y, powerup), vida(V), VV is 100,
				 retract(tile(X,Y,powerup)), assert(tile(X,Y,nada)),
				 retract(vida(V)), assert(vida(VV)).
% Localizar powerup
guardar_powerup :- posicao(X, Y, _), tile(X, Y, powerup),
		 		   retract(conhecimento(X, Y, _, _)), assert(conhecimento(X, Y, powerup, 1)).


% Ações, em ordem de preferência

	% Escapar
acao(A) :- amigos_salvos(AS), AS = 3, A = escapar.

	% Salvar o Amigo
acao(A) :- posicao(X, Y, _), tile(X, Y, amigo), A = salvar_amigo.

	% Pegar Power Up
acao(A) :- posicao(X, Y, _), tile(X, Y, powerup), vida(V), V < 51, A = pegar_powerup.

	% Guarda Posição Power Up
acao(A) :- posicao(X, Y, _), tile(X, Y, powerup), not(conhecimento(X, Y, powerup, _)), A = guardar_powerup.

	% Virar-se caso esteja de frente para uma parede
acao(A) :- posicao(_,Y,P), P = norte, Y = 0, A = virar_direita,!.
acao(A) :- posicao(_,Y,P), P = sul, Y = 11, A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = leste, X = 11, A = virar_direita,!.
acao(A) :- posicao(X,_,P), P = oeste, X = 0, A = virar_direita,!.

	% Buscar Power Up
acao(A) :- vida(V), V < 51, A = buscar_powerup.

	% Andar para não visitado que tenha pedido_ajuda
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, pedido_ajuda, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, pedido_ajuda, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, pedido_ajuda, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, pedido_ajuda, 0), A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que tenha pedido_ajuda
acao(A) :- posicao(X,Y,_), adjacente(X, Y, AX, AY),
		   conhecimento(AX, AY, pedido_ajuda, 0), A = virar_direita,!.

	% Andar para não visitado que tenha pedido_ajuda
acao(A) :- conhecimento(_,_,pedido_ajuda,0), A = buscar_amigo,!.

	% Andar para não visitado que tenha pedido_ajuda, mesmo com brisa
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, [brisa, pedido_ajuda], 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, [brisa, pedido_ajuda], 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, [brisa, pedido_ajuda], 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, [brisa, pedido_ajuda], 0), A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que tenha pedido_ajuda, mesmo com brisa
acao(A) :- posicao(X,Y,_), adjacente(X, Y, AX, AY),
		   conhecimento(AX, AY, [brisa, pedido_ajuda], 0), A = virar_direita,!.

	% Andar para não visitado que não tenha nada
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, nada, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, nada, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, nada, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, nada, 0), A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que não tenha nada 
acao(A) :- posicao(X,Y,_), adjacente(X, Y, AX, AY),
           conhecimento(AX, AY, nada, 0), A = virar_direita.

    % Buscar caminho livre se ainda existir
acao(A) :- conhecimento(_,_,S,0), (S=pedido_ajuda;S=nada), A = buscar_livre.

	% Atacar Monstro caso não tenha mais não visitado que não tenha nada
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1, municao(M), M > 0,
		   conhecimento(X, YY, T, 0), (T = passos; T = som_asas), A = atacar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1, municao(M), M > 0,
		   conhecimento(X, YY, T, 0), (T = passos; T = som_asas), A = atacar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1, municao(M), M > 0,
		   conhecimento(XX, Y, T, 0), (T = passos; T = som_asas), A = atacar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1, municao(M), M > 0,
		   conhecimento(XX, Y, T, 0), (T = passos; T = som_asas), A = atacar,!.

	% Virar-se caso tenha algum adjacente com monstro
acao(A) :- posicao(X,Y,_), adjacente(X, Y, AX, AY), municao(M), M > 0,
           conhecimento(AX, AY, T, _), (T = passos; T = som_asas), A = virar_direita.

    % Buscar monstro para atacar se houver
acao(A) :- conhecimento(_,_,T,_), (T = passos; T = som_asas), municao(M), M > 0, A = buscar_monstro.

	% Andar só em monstros que vão me acrescentar em algo...

	% Andar por monstro se tiver vida, mas não munição 
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1, vida(V), V > 50,
		   conhecimento(X, YY, passos, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11, YY is Y + 1, vida(V), V > 50,
		   conhecimento(X, YY, passos, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1, vida(V), V > 50,
		   conhecimento(XX, Y, passos, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0, XX is X - 1, vida(V), V > 50,
		   conhecimento(XX, Y, passos, _), A = andar,!.

	% Virar-se caso tenha algum adjacente com monstro para andar
acao(A) :-  posicao(X,Y,_), adjacente(X, Y, AX, AY), vida(V), V > 50,
			conhecimento(AX, AY, passos, _), A = virar_direita.

	% Buscar monstro para andar caso tenha vida
acao(A) :- conhecimento(_,_,passos,_), vida(V), V > 50, A = buscar_monstro.

	% Andar para não visitado que tenha teletransporte
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, som_asas, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, som_asas, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, som_asas, _), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, som_asas, _), A = andar,!.

	% Virar-se caso tenha algum adjacente não visitado que tenha teletransporte
acao(A) :- posicao(X,Y,_), adjacente(X, Y, AX, AY),
           conhecimento(AX, AY, som_asas, _), A = virar_direita,!.

    % Buscar por Teletransporte se não tiver outra opção
acao(A) :- conhecimento(_,_,som_asas,_), A = buscar_teletransporte.

    % Arriscar andar em possível buraco se não tiver mais opção
acao(A) :- posicao(X,Y,P), P = norte, Y > 0, YY is Y - 1,
		   conhecimento(X, YY, brisa, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = sul, Y < 11,  YY is Y + 1,
		   conhecimento(X, YY, brisa, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = leste, X < 11, XX is X + 1,
		   conhecimento(XX, Y, brisa, 0), A = andar,!.
acao(A) :- posicao(X,Y,P), P = oeste, X > 0,  XX is X - 1,
		   conhecimento(XX, Y, brisa, 0), A = andar,!.