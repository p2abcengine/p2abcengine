/*
 * Copyright 2013 CryptoExperts
 *
 * This file is part of the MULTOS iplementation for ABC4Trust
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#define MAX(a,b) (((a) > (b)) ? (a) : (b))

#define MIN(a,b) (((a) < (b)) ? (a) : (b))

#define exitSW(w12) \
  do { SW12 = w12; __code(__SYSTEM, 4); } while (0)

#define exitLa(la) \
  do { La = la; __code(__SYSTEM, 4); } while (0)

#define exitSWLa(sw, la) \
  do { __code(__SYSTEM, 7, sw, la); } while (0)

#define SHA256(digest, plaintext_length, plaintext)	\
do {							\
  __push(__typechk(unsigned int, plaintext_length));	\
  __push(__typechk(unsigned int, 32));			\
  __push(__typechk(unsigned char *, digest));	\
  __push(__typechk(unsigned char *, plaintext)); 	\
  __code(__PRIM, 0xcf);					\
} while (0)

#define crxModularExponentiation(exponentLength, modulusLength, exponent, modulus, input, output) \
  if (exponentLength > modulusLength) exitSW(0x9F30); \
  multosModularExponentiation(exponentLength, modulusLength, exponent, modulus, input, output)

#define crxAESEncryptCBC(ciphertext, iv, plaintext, plaintext_size, key) \
do \
{ \
    __push (__typechk(BYTE, 0x10)); \
    __push (__typechk(BYTE *, iv)); \
    __push (__typechk(WORD, plaintext_size)); \
    __push (__typechk(BYTE *, key)); \
    __push (__typechk(BYTE, 0x10)); \
    __push (__typechk(BYTE *, ciphertext)); \
    __push (__typechk(BYTE *, plaintext)); \
    __code (__PRIM, 0xDB, 0x06, 0x02); \
} while (0)

// crxAESDecryptCBC(BYTE *plaintext, const BYTE *iv, const BYTE *ciphertext, WORD ciphertext_size, const BYTE *key)
#define crxAESDecryptCBC(plaintext, iv, ciphertext, ciphertext_size, key) \
do \
{ \
    __push (__typechk(BYTE, 0x10)); \
    __push (__typechk(BYTE *, iv)); \
    __push (__typechk(WORD, ciphertext_size)); \
    __push (__typechk(BYTE *, key)); \
    __push (__typechk(BYTE, 0x10)); \
    __push (__typechk(BYTE *, plaintext)); \
    __push (__typechk(BYTE *, ciphertext)); \
    __code (__PRIM, 0xDA, 0x06, 0x02); \
} while (0)


/* 
 * crxPushCarry()
 *
 */

#define crxPushCarry()						\
  do {								\
    __code (__PRIM, 0x05);					\
    __code (__PRIM, 0x01, 0x83, 0x08);				\
    __code (__PRIM, 0x03, 0x01, 0x03);				\
  } while (0)

/* 
 * crxPopByte(adr)
 *
 */

#define crxPopByte(adr)						\
  do {								\
    __code (0x08, (adr), 1);					\
  } while (0)


#define crxPushByte(adr)					\
  do {								\
    __code (0x07, (adr), 1);					\
  } while (0)


/*
 * crxBlockMultiply127orLess(blockLength, block1, block2, result)
 *
 */

#define crxBlockMultiply127orLess(blockLength, block1, block2, result) \
do \
{ \
  __push (__BLOCKCAST(blockLength)(__typechk(BYTE *, (block1)))); \
  __push (__BLOCKCAST(blockLength)(__typechk(BYTE *, (block2)))); \
  __code (__PRIM, __PRIM_MULTIPLYN, (blockLength));		  \
  __code (__STORE, __typechk(BYTE *, (result)), ((blockLength)*2)); \
} while (0)

/*
 * crxBlockMultiply128(block1, block2, result)
 *
 * block1 should be able to store 128 bytes *after* the 128 input
 * bytes (i.e., block1 is 256 bytes long but the input data is stored
 * on the 128 first bytes). Same thing for block2.
 * The 256 bytes of block 1 cannot overlap the 256 bytes of block 2.
 * result can point to any place (even on block1, or block1 + 1, etc.)
 */

#define crxBlockMultiply128(block1, block2, result)			\
  									\
  memset((block1) + 128, 0, 64);					\
  memset((block1) + 192, 0, 64);					\
  memset((block2) + 128, 0, 64);					\
  memset((block2) + 192, 0, 64);					\
  									\
  crxBlockMultiply127orLess(64, (block1) + 64, (block2) + 64, (block1) + 128); \
  crxBlockMultiply127orLess(64, (block1) + 64, (block2)     , (block2) + 128); \
  									\
  memset((block1) + 64, 0, 64);						\
  									\
  multosBlockAdd(128, (block1) + 64, (block2) + 128, (block1) + 64);	\
  									\
  crxBlockMultiply127orLess(64, (block1), (block2) + 64, (block2) + 128); \
  									\
  multosBlockAdd(128, (block1) + 64, (block2) + 128, (block1) + 64);	\
  									\
  crxPushCarry();							\
  									\
  crxBlockMultiply127orLess(64, (block1), (block2), (block2) + 128);	\
  memset((block1), 0, 64);						\
  crxPopByte((block1) + 63);						\
  									\
  multosBlockAdd(128, (block1), (block2) + 128, (block1));		\
  									\
  if ((result) != (block1)) {						\
    memcpy((result), (block1), 256);					\
  }									

/*
 * crxBlockMultiply256(block1, block2, result)
 *
 * block1 should be able to store 256 bytes *after* the 256 input
 * bytes (i.e., block1 is 512 bytes long but the input data is stored
 * on the 256 first bytes). Similar for block2, except that we only
 * need 128 bytes after the 256 bytes of input.  The 512 bytes of
 * block 1 cannot overlap the 384 bytes of block 2.  result can point
 * to any place (even on block1, or block1 + 1, etc.)
 */

#define crxBlockMultiply256(block1, block2, result)			\
  									\
  memset((block1) + 256, 0, 64);					\
  memset((block1) + 320, 0, 64);					\
  memset((block1) + 384, 0, 64);					\
  memset((block1) + 448, 0, 64);					\
  memset((block2) + 256, 0, 64);					\
  memset((block2) + 320, 0, 64);					\
  									\
  crxBlockMultiply127orLess(64, (block1) + 192, (block2) + 192, (block1) + 384); \
									\
  crxBlockMultiply127orLess(64, (block1) + 192, (block2) + 128, (block2) + 256); \
  multosBlockAdd(128, (block1) + 320, (block2) + 256, (block1) + 320);	\
									\
  crxBlockMultiply127orLess(64, (block1) + 128, (block2) + 192, (block2) + 256); \
  multosBlockAdd(128, (block1) + 320, (block2) + 256, (block1) + 320);	\
  crxPushCarry();							\
  crxPopByte((block1) + 319);						\
									\
  crxBlockMultiply127orLess(64, (block1) + 192, (block2) + 64, (block2) + 256); \
  multosBlockAdd(128, (block1) + 256, (block2) + 256, (block1) + 256);	\
									\
  crxBlockMultiply127orLess(64, (block1) + 128, (block2) + 128, (block2) + 256); \
  multosBlockAdd(128, (block1) + 256, (block2) + 256, (block1) + 256);	\
  crxPushCarry();							\
									\
  crxBlockMultiply127orLess(64, (block1) + 64, (block2) + 192, (block2) + 256); \
  multosBlockAdd(128, (block1) + 256, (block2) + 256, (block1) + 256);	\
  crxPushCarry();							\
									\
  crxPopByte((block2) + 256);						\
  crxPopByte((block2) + 257);						\
  (block2)[256] = (block2)[256] + (block2)[257];			\
  crxPushByte((block2) + 256);						\
									\
  crxBlockMultiply127orLess(64, (block1) + 192, (block2), (block2) + 256); \
  memset((block1)+192, 0, 64);						\
  crxPopByte((block1)+255);						\
  multosBlockAdd(128, (block1) + 192, (block2) + 256, (block1) + 192);	\
									\
  crxBlockMultiply127orLess(64, (block1) + 128, (block2) + 64, (block2) + 256); \
  multosBlockAdd(128, (block1) + 192, (block2) + 256, (block1) + 192);	\
  crxPushCarry();							\
									\
  crxBlockMultiply127orLess(64, (block1) + 64, (block2) + 128, (block2) + 256); \
  multosBlockAdd(128, (block1) + 192, (block2) + 256, (block1) + 192);	\
  crxPushCarry();							\
									\
  crxBlockMultiply127orLess(64, (block1), (block2) + 192, (block2) + 256); \
  multosBlockAdd(128, (block1) + 192, (block2) + 256, (block1) + 192);	\
  crxPushCarry();							\
									\
  crxPopByte((block2) + 256);						\
  crxPopByte((block2) + 257);						\
  crxPopByte((block2) + 258);						\
  (block2)[256] = (block2)[256] + (block2)[257] + (block2)[258];	\
  crxPushByte((block2) + 256);						\
									\
  crxBlockMultiply127orLess(64, (block1) + 128, (block2), (block2) + 256); \
  memset((block1)+128, 0, 64);						\
  crxPopByte((block1)+191);						\
  multosBlockAdd(128, (block1) + 128, (block2) + 256, (block1) + 128);	\
									\
  crxBlockMultiply127orLess(64, (block1) + 64, (block2) + 64, (block2) + 256); \
  multosBlockAdd(128, (block1) + 128, (block2) + 256, (block1) + 128);	\
  crxPushCarry();							\
									\
  crxBlockMultiply127orLess(64, (block1), (block2) + 128, (block2) + 256); \
  multosBlockAdd(128, (block1) + 128, (block2) + 256, (block1) + 128);	\
  crxPushCarry();							\
									\
  crxPopByte((block2) + 256);						\
  crxPopByte((block2) + 257);						\
  (block2)[256] = (block2)[256] + (block2)[257];			\
  crxPushByte((block2) + 256);						\
									\
  crxBlockMultiply127orLess(64, (block1) + 64, (block2), (block2) + 256); \
  memset((block1)+64, 0, 64);						\
  crxPopByte((block1)+127);						\
  multosBlockAdd(128, (block1) + 64, (block2) + 256, (block1) + 64);	\
									\
  crxBlockMultiply127orLess(64, (block1), (block2) + 64, (block2) + 256); \
  multosBlockAdd(128, (block1) + 64, (block2) + 256, (block1) + 64);	\
  crxPushCarry();							\
									\
  crxBlockMultiply127orLess(64, (block1), (block2), (block2) + 256);	\
  memset((block1), 0, 64);						\
  crxPopByte((block1)+63);						\
  multosBlockAdd(128, (block1), (block2) + 256, (block1));		\
  									\
  if ((result) != (block1)) {						\
    memcpy((result), (block1), 512);					\
  }									


/*
 * crxBlockAdd(const BYTE blockLength, BYTE *block1, BYTE *block2, const BYTE *result)
 *
 * Warning: result[0..254] should not overlap either block1[255] or block2[255]
 */

#define crxBlockAdd(blockLength, block1, block2, result) 		\
    if ((blockLength) != 0 && (blockLength) != 256) {			\
      multosBlockAdd((blockLength), (block1), (block2), (result));	\
    } else {								\
      multosBlockAdd(255, (block1), (block2), (result));		\
      if (((block1)[255] + (block2)[255]) > 255) {			\
	multosBlockIncrement(255, (result));				\
      }									\
      (result)[255] = (block1)[255] + (block2)[255];			\
    }

/*
 * crxBlockSubtract(const BYTE blockLength, BYTE *block1, BYTE *block2, const BYTE *result)
 *
 * This assumes that block1 >= block2
 * Warning: result[0..254] should not overlap either block1[255] or block2[255]
 */

#define crxBlockSubtract(blockLength, block1, block2, result) 		\
    if ((blockLength) != 0 && (blockLength) != 256) {			\
      multosBlockSubract((blockLength), (block1), (block2), (result));	\
    } else {								\
      multosBlockSubract(255, (block1), (block2), (result));		\
      if ((block1)[255] >= (block2)[255]) {				\
	(result)[255] = (block1)[255] - (block2)[255];			\
      } else {								\
	(result)[255] = (block1)[255] + (256 - (block2)[255]);		\
	multosBlockDecrement(255, (result));				\
      }									\
    }

/*
 * void crxModularMultiplication (WORD mLength, BYTE *m, BYTE *a, BYTE *b)
 *
 * We expect both 'a' and 'b' to be already reduced modulo 'm'.
 *
 * We expect 'a' to be a buffer of size BUFFER_MAX_SIZE, the
 * significant bytes being stored on 
 *
 * a[MAX_BIGINT_SIZE-mLength ... MAX_BIGINT_SIZE].
 * 
 * Same for 'b'.
 *
 * The result is stored in
 *
 * a[MAX_BIGINT_SIZE-mLength ... MAX_BIGINT_SIZE].
 *
 * We expect 'm' to be a buffer of size MAX_BIGINT_SIZE, the
 * significant bytes being stored on 
 *
 * m[MAX_BIGINT_SIZE-mLength ... MAX_BIGINT_SIZE]. 
 *
 */

#define crxModularMultiplication(mLength, m, a, b) \
  						   \
  i = (a)[MAX_BIGINT_SIZE-1];						\
  crxBlockSubtract (MAX_BIGINT_SIZE, (m), (a), (a)+MAX_BIGINT_SIZE);	\
  									\
  multosModularMultiplication((mLength), (m)+MAX_BIGINT_SIZE-(mLength), (a) + MAX_BIGINT_SIZE - (mLength), (b) + MAX_BIGINT_SIZE - (mLength)); \
  									\
  if (i == (a)[MAX_BIGINT_SIZE-1]) {					\
    									\
    i = (a)[2*MAX_BIGINT_SIZE-1];					\
    multosModularMultiplication((mLength), (m)+MAX_BIGINT_SIZE-(mLength), (a) + 2*MAX_BIGINT_SIZE - (mLength), (b) + MAX_BIGINT_SIZE - (mLength)); \
    									\
    if (i != (a)[2*MAX_BIGINT_SIZE-1]) {				\
      crxBlockSubtract (MAX_BIGINT_SIZE, (m), (a)+MAX_BIGINT_SIZE, (a)); \
    } else {								\
      memcpy((b)+MAX_BIGINT_SIZE, (a), MAX_BIGINT_SIZE);		\
      memset((b), 0, MAX_BIGINT_SIZE);					\
									\
      i = 1;								\
      /* (0,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (0,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE - 1, MAX_BIGINT_SIZE);	\
	}								\
      }									\
      /* (1,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (1,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-1, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (0,2) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE-2, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-2, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (2,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-2, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (2,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-2, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-1, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (1,2) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE-2, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-2, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (3,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-3, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (0,3) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE-3, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-3, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (3,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-3, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-1, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (1,3) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE-3, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-3, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (4,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-4, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (0,4) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE-4, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-4, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (4,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-4, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-1, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (1,4) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE-4, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-4, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (5,0) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-5, (b)+MAX_BIGINT_SIZE, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (0,5) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE, (b)+MAX_BIGINT_SIZE-5, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-5, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (5,1) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-5, (b)+MAX_BIGINT_SIZE-1, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-1, MAX_BIGINT_SIZE);		\
	}								\
      }									\
      /* (1,5) */							\
      if(i) {								\
	memset((a), 0, MAX_BIGINT_SIZE);				\
	crxBlockAdd(MAX_BIGINT_SIZE, (a)+MAX_BIGINT_SIZE-1, (b)+MAX_BIGINT_SIZE-5, (a)); \
	if (memcmp((a),(m), MAX_BIGINT_SIZE) == 0) {			\
	  i = 0;							\
	  memcpy((a), (b)+MAX_BIGINT_SIZE-5, MAX_BIGINT_SIZE);		\
	}								\
      }									\
									\
    }									\
									\
  }


void getRandomBytes(BYTE* buffer, unsigned int size);
void checkPin(unsigned char* tested_pin);
void checkPuk(unsigned char* tested_puk);
unsigned int sizeDecode(unsigned char *s);
void sizeEncode(unsigned char *s, unsigned int size);
void getKey(BYTE *key, unsigned int *key_size, const BYTE key_id);
void publicKeyEncrypt(unsigned char* key, unsigned int key_size);
void encryption(BYTE* dst, unsigned int* dst_size, const BYTE *src, const unsigned int src_size, const BYTE *key, const unsigned int key_size);
void extract(const BYTE *key, const unsigned int key_size);
unsigned int extraction(const BYTE *n, const unsigned int n_size, BYTE *sig, unsigned int *sig_size, const BYTE *challenge, const unsigned int challenge_size);
void checkBufferPrefix(BYTE ins, BYTE *datain, unsigned int datain_size);
void checkBufferEqual(BYTE ins, BYTE *datain, unsigned int datain_size);
BYTE groupExists(BYTE group_id);
void getGroupComponent(BYTE group_id, BYTE comptype);
BYTE generatorExists(BYTE group_id, BYTE gen_id);
void getGenerator(BYTE group_id, BYTE gen_id);
BYTE accessCredential(BYTE *pin, BYTE credential_id);
void singleOrDoubleExpo(BYTE issuer_id, BYTE *e1, unsigned int e1_size, BYTE *e2, unsigned int e2_size);
void accessSession(BYTE credential_id);
void singleOrDoubleResponse(BYTE issuer_id, BYTE *c, unsigned int c_size, BYTE *x, unsigned int x_size, BYTE *kx, unsigned int kx_size, BYTE *v, unsigned int v_size, BYTE *kv, unsigned int kv_size);
void singleResponse(BYTE *k, unsigned int k_size, BYTE *c, unsigned int c_size, BYTE *u, unsigned int u_size, BYTE *q, unsigned int q_size, BYTE offset);
void scopeExclusiveGenerator(BYTE *scope, unsigned int scope_size, BYTE *m, unsigned int m_size, BYTE *f, unsigned int f_size);
BYTE* accessURI(BYTE *datain, unsigned int Lc);
void getBlobstoreInformations(unsigned int* first_available_index, unsigned int *blobcount, unsigned int *uri_index, unsigned char *uri, BYTE uri_size);
void encrypt(BYTE *password, BYTE label);
void decrypt(BYTE *device_id_prim, BYTE *password, BYTE label);
void print(void *s, unsigned int size);
void output_large_data(void);
void staticHighToSegment(void *low_addr, const void *high_addr, size_t size);
void segmentToStaticHigh(void *high_addr, const void *low_addr, size_t size);

