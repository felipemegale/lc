sseg segment stack         ; inicio seg pilha
byte 4000h DUP(?)          ; dimensiona pilha
sseg ends                  ; fim seg pilha

dseg segment public        ; inicio seg dados
byte 4000h DUP(?)          ; temporarios
sword 6 DUP(?)         ; 4000
sword ?          ; endereco atual: 4006
sword ?          ; endereco atual: 4008
sword 1         ; endereco atual: 4010
byte 'a'         ; endereco atual: 4012
byte 10h DUP(?)         ; 4013
byte ?         ; endereco atual: 4023
dseg ends                  ; fim seg dados

cseg segment public        ; inicio seg codigo
assume CS:cseg, DS:dseg
strt:                      ; inicio do programa
mov ax, dseg
mov ds, ax
mov ax, 0 	; Armazenando valor inteiro em ax
mov DS:[0h], ax	; Salvando em Fator.end
Rot1:
mov ax, 2 	; Armazenando valor inteiro em ax
mov DS:[0h], ax	; Salvando em Fator.end
mov ax, DS:[0h]	; Pegando conteudo de exp.end
mov bx, DS:[4006h]	; Pegando conteudo de id.end
mov ah, 0
mov bh, 0
cmp ax, bx	; comparando ax bx
jg Rot2
dseg segment public	; Inicio segmento de dados
byte "digite um numero: $"	; Armazenando valor String
dseg ENDS	; Final segmento de dados
dseg segment public	; Inicio segmento de dados
byte "felipe$"	; Armazenando valor String
dseg ENDS	; Final segmento de dados
mov dx, 0h 	; Obtendo endereco de buffer
mov al, 3h	; tamanho do vetor ou 255
mov DS:[0h], al	; Armazenando tamanho
mov ah, 0Ah
int 21h	; interrupcao para leitura
mov ah, 02h	; quebra de linha
mov dl, 0Dh
 int 21h
mov DL, 0Ah
int 21h
mov di, [2h]	; posicao do string
mov ax, 0
mov cx, 10
mov dx, 1
mov bh, 0
mov bl, DS:[di]
cmp bx, 2Dh
jne r0
mov dx, -1
add di, 1
mov bl, ds:[di]
r0:
push dx
mov dx, 0
r1: 
cmp bx, 0dh
je r2
imul cx
add bx, -48
add ax, bx
add di, 1
mov bh, 0
mov bl, ds:[di]
jmp r1
r2:
pop cx
imul cx
mov ax, DS:[4006h]	; Pegando conteudo de id.end
add ax, 1
jmp Rot1
Rot2:
mov ax, 0 	; Armazenando valor inteiro em ax
mov DS:[0h], ax	; Salvando em Fator.end
mov BX, DS:[0h]	; Obtendo valor de Exp1.end(Posicao)
add BX, BX	; Inteiro (2Bytes)!
add BX, 4000h	; Somando Posicao + id.end(inicial)
mov AX, DS:[BX]	; Carregando valor na pos correta
mov DS:[2h], AX	; Armazenando em Fator.end
mov ax, 3 	; Armazenando valor inteiro em ax
mov DS:[4h], ax	; Salvando em Fator.end
mov BX, DS:[4h]	; Obtendo valor de Exp1.end(Posicao)
add BX, BX	; Inteiro (2Bytes)!
add BX, 4000h	; Somando Posicao + id.end(inicial)
mov AX, DS:[BX]	; Carregando valor na pos correta
mov DS:[6h], AX	; Armazenando em Fator.end
mov ax, DS:[2h]	; (ax) <- Carrega valor de exp.end
mov bx, DS:[2h]	; (bx) <- Carrega valor de expS.end
mov ah, 0	; Convertendo AX para inteiro (ah = 0)
mov bh, 0	; Convertendo BX para inteiro (bh = 0)
cmp ax, bx	; Comparando ax com bx
jg Rot5	; Pula para + Rot5 caso ax > bx
mov ax, 0	; armazena como FALSO
jmp Rot6	; Pula para + Rot6 (fim)
Rot5:
 mov ax, 1	; armazena como VERDADEIRO
Rot6:
mov DS:[8h], ax	; Armazenando resultado em Exp.end
mov ax, DS:[8h]	; Carrega valor de Exp.end
cmp ax, 0 	; Verificando se Exp e' falso
je Rot3
dseg segment public	; Inicio segmento de dados
byte "teste$"	; Armazenando valor String
dseg ENDS	; Final segmento de dados
jmp Rot4	; Pulando para o final
Rot3:
dseg segment public	; Inicio segmento de dados
byte "o maior numero e' $"	; Armazenando valor String
dseg ENDS	; Final segmento de dados
Rot4:

mov ah, 4Ch
int 21h
cseg ends                  ; fim seg codigo
end strt                   ; fim programa