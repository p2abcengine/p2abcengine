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

#include <multos.h>
#include <stdlib.h>
#include <string.h>
#include <main.h>

#pragma attribute("aid", "41 42 43 34 54")

#ifdef TEST_MODE
#pragma attribute("dir", "61 1C 4f 05 41 42 43 34 54 50 19 41 42 43 34 54 72 75 73 74 20 4c 69 74 65 20 76 32 2e 30 20 44 65 62 75 67") // dir format : 61 Len 4f Len AID 50 Len Label
#else
#pragma attribute("dir", "61 1C 4f 05 41 42 43 34 54 50 13 41 42 43 34 54 72 75 73 74 20 4c 69 74 65 20 76 32 2e 30") // dir format : 61 Len 4f Len AID 50 Len Label
#endif

#define ABC_CLA 0xBC

/************************************************************************
 * INS Bytes Definitions
 ************************************************************************/

#ifdef TEST_PATRAS_MODE
#define INS_GET_KX_AND_DEVICE_KEY          0x00
#define INS_TEST                           0x01
#endif
#define INS_GET_MODE                       0x02
#define INS_SET_ROOT_MODE                  0x04
#define INS_SET_WORKING_MODE               0x06
#define INS_SET_VIRGIN_MODE                0x08
#define INS_SET_FAST_VIRGIN_MODE           0x09
#define INS_PIN_TRIALS_LEFT                0x0A
#define INS_PUK_TRIALS_LEFT                0x0C
#define INS_CHANGE_PIN                     0x0E
#define INS_RESET_PIN                      0x10
#define INS_INITIALIZE_DEVICE              0x12
#define INS_GET_DEVICE_ID                  0x14
#define INS_GET_VERSION                    0x16
#define INS_PUT_DATA                       0x1A
#define INS_GET_CHALLENGE                  0x1C
#define INS_AUTHENTICATE_DATA              0x1E
#define INS_SET_AUTHENTICATION_KEY         0x20
#define INS_LIST_AUTHENTICATION_KEYS       0x22
#define INS_READ_AUTHENTICATION_KEY        0x24
#define INS_REMOVE_AUTHENTICATION_KEY      0x26
#define INS_SET_GROUP_COMPONENT            0x28
#define INS_SET_GENERATOR                  0x2A 
#define INS_LIST_GROUPS                    0x2C
#define INS_READ_GROUP                     0x2E
#define INS_READ_GROUP_COMPONENT           0x30
#define INS_READ_GENERATOR                 0x32
#define INS_REMOVE_GROUP                   0x34
#define INS_SET_COUNTER                    0x36
#define INS_INCREMENT_COUNTER              0x38
#define INS_LIST_COUNTERS                  0x3A
#define INS_READ_COUNTER                   0x3C
#define INS_REMOVE_COUNTER                 0x3E
#define INS_SET_ISSUER                     0x40
#define INS_LIST_ISSUERS                   0x42
#define INS_READ_ISSUER                    0x44
#define INS_REMOVE_ISSUER                  0x46
#define INS_SET_PROVER                     0x48
#define INS_READ_PROVER                    0x4A
#define INS_REMOVE_PROVER                  0x4C
#define INS_START_COMMITMENTS              0x4E
#define INS_START_RESPONSES                0x50
#define INS_SET_CREDENTIAL                 0x52
#define INS_LIST_CREDENTIALS               0x54
#define INS_READ_CREDENTIAL                0x56
#define INS_REMOVE_CREDENTIAL              0x58
#define INS_GET_CREDENTIAL_PUBLIC_KEY      0x5A
#define INS_GET_ISSUANCE_COMMITMENT        0x5C
#define INS_GET_ISSUANCE_RESPONSE          0x5E
#define INS_GET_PRESENTATION_COMMITMENT    0x60
#define INS_GET_PRESENTATION_RESPONSE      0x62
#define INS_GET_DEVICE_PUBLIC_KEY          0x64
#define INS_GET_DEVICE_COMMITMENT          0x66
#define INS_GET_DEVICE_RESPONSE            0x68
#define INS_GET_SCOPE_EXCLUSIVE_PSEUDONYM  0x6A
#define INS_GET_SCOPE_EXCLUSIVE_COMMITMENT 0x6C
#define INS_GET_SCOPE_EXCLUSIVE_RESPONSE   0x6E
#define INS_STORE_BLOB                     0x70
#define INS_LIST_BLOBS                     0x72
#define INS_READ_BLOB                      0x74
#define INS_REMOVE_BLOB                    0x76
#define INS_BACKUP_DEVICE                  0x78
#define INS_RESTORE_DEVICE                 0x7A
#define INS_BACKUP_COUNTERS                0x7C
#define INS_RESTORE_COUNTERS               0x7E
#ifdef SODER
#define INS_BACKUP_CREDENTIAL              0x80
#define INS_RESTORE_CREDENTIAL             0x82
#endif
#define INS_GET_INFO                       0x84
#define INS_NOTHING                        0xBC
#define INS_GET_RESPONSE                   0xC0

/************************************************************************
 * ERR Bytes Definitions
 ************************************************************************/

#define ERR_OK                                          0x9000
#define ERR_BAD_MODE                                    0x9A00 /* actually return 0x9Axx where xx is the current mode */
#define ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA        0x9B00 /* actually return 0x9Bxx where xx is the minimum expected size */
#define ERR_INCORRECT_SIZE_OF_INCOMMING_DATA            0x9C00 /* actually return 0x9Cxx where xx is the exact expected size */
#define ERR_INCORRECT_MIN_SIZE_OF_RAND                  0x9D00 /* actually return 0x9Dxx where xx is the minimum expected size of the random challenge */
#define ERR_INTEGER_EXCEEDS_MAXINTSIZE                  0x9F01
#define ERR_FAILED_ACCESS_PRIVILEGED_MODE               0x9F02
#define ERR_INCORRECT_PIN                               0x9F03
#define ERR_INCORRECT_PIN_AND_CARD_LOCKED               0x9F04
#define ERR_INCORRECT_PUK                               0x9F05
#define ERR_INCORRECT_PUK_AND_CARD_DEAD                 0x9F06
#define ERR_DEVICE_KEY_SHORTER_THAN_MIN_X_SIZE          0x9F07
#define ERR_KEY_ID_OUTSIDE_RANGE                        0x9F08
#define ERR_AUTHENTICATION_KEY_DOES_NOT_EXIST           0x9F09
#define ERR_DATA_AUTHENTICATED_WRT_NON_ROOT_KEY         0x9F0A
#define ERR_NEED_PRIOR_DATA_AUTHENTICATION_WRT_ROOT_KEY 0x9F0B
#define ERR_DATA_AUTHENTICATION_FAILURE                 0x9F0C
#define ERR_CMD_PARAMETERS_FAILED_ROOT_AUTHENTICATION   0x9F0D
#define ERR_AUTHENTICATION_KEY_TOO_SHORT                0x9F0E
#define ERR_GROUPID_OUTSIDE_OF_RANGE                    0x9F0F
#define ERR_GROUP_DOES_NOT_EXIST                        0x9F10
#define ERR_ID_OF_GROUP_GENERATOR_OUTSIDE_OF_RANGE      0x9F11
#define ERR_GENERATOR_DOES_NOT_EXIST                    0x9F12
#define ERR_COMPONENT_TYPE_OUTSIDE_OF_RANGE             0x9F13
#define ERR_COUNTER_ID_OUTSIDE_OF_RANGE                 0x9F14
#define ERR_COUNTER_DOES_NOT_EXIST                      0x9F15
#define ERR_ISSUERID_OUTSIDE_OF_RANGE                   0x9F16
#define ERR_ISSUER_DOES_NOT_EXIST                       0x9F17
#define ERR_PROVERID_OUTSIDE_OF_RANGE                   0x9F18
#define ERR_PROVER_DOES_NOT_EXIST                       0x9F19
#define ERR_ONE_ID_OF_CREDIDS_IS_OUTSIDE_OF_RANGE       0x9F1A
#define ERR_CREDENTIALID_OUTSIDE_OF_RANGE               0x9F1B
#define ERR_CREDENTIAL_DOES_NOT_EXIST                   0x9F1C
#define ERR_PROOF_SESSION_CANNOT_START                  0x9F1D
#define ERR_MALICIOUS_INPUT_RESPONSE_STAGE              0x9F1E
#define ERR_CURRENT_PROOF_SESSION_INAP_STAGE            0x9F1F
#define ERR_CREDENTIAL_INAP_STATE                       0x9F20
#define ERR_CREDENTIAL_OR_PSEUDO_NOT_MEMBER_PROOF_SESS  0x9F21
#define ERR_PRESENTATION_CRED_RESTRICTED_BY_IMM_COUNTER 0x9F22
#define ERR_URI_TOO_LARGE                               0x9F23
#define ERR_URI_CONTAINS_INVALID_CHARS                  0x9F24
#define ERR_NO_BLOB_WITH_GIVEN_URI                      0x9F25
#define ERR_BLOB_TOO_LARGE                              0x9F26
#define ERR_INVALID_BACKUP_ARCHIVE                      0x9F27
#define ERR_NO_CONTENT_TO_BACKUP                        0x9F28
#define ERR_MAX_NBR_BLOB_REACHED                        0x9F29
#define ERR_EXPONENT_LARGER_THAN_MODULUS                0x9F30 // if this changed, check the crxModularExponentiation routine in main.h

#define ERR_BAD_ISO     0x6700
#define ERR_BAD_CLA     0x6e00
#define ERR_BAD_INS     0x6d00

#define CRX_ERROR       0x9FFF

/************************************************************************
 * Other constants
 ************************************************************************/

#define MASTER_BACKUP_KEY_SIZE 16

#define MODE_VIRGIN    0x00
#define MODE_ROOT      0x01
#define MODE_WORKING   0x02
#define MODE_LOCKED    0x03
#define MODE_DEAD      0x04

#define ACCESS_CODE_SIZE 8
#define PIN_SIZE 4
#define PUK_SIZE 8
#define MAC_SIZE 16
#define PASSWORD_SIZE 8
#define CHALLENGE_MAX_SIZE 224
#define SMALL_BUFFER_MAX_SIZE 32 // Must be >= 32
#define BUFFER_MAX_SIZE 512
#define RESURRECTION_KEY_SIZE 16
#define ID_SIZE 2
#define KEY_ID_SIZE 1
#define PROVER_ID_SIZE 1
#define SIZE_SIZE 2 // change this at your own risk

#ifdef TEST_MODE
  #define NUM_ISSUERS 3
  #define MAX_NUMBER_OF_BLOBS 10
  #define NUM_GROUPS ((NUM_ISSUERS)+1)
  #define NUM_COUNTERS (NUM_ISSUERS)
  #define NUM_AUTH_KEYS ((NUM_ISSUERS)+1)
  #define NUM_CREDS 20
  #define NUM_PROVERS 2
  #define NUM_GEN 3
#else
  #ifdef SODER
    #define NUM_ISSUERS 6
    #define MAX_NUMBER_OF_BLOBS 34 // was 40 in ALU 11, was 39
    #define NUM_GROUPS ((NUM_ISSUERS)+1)
    #define NUM_COUNTERS 0
    #define NUM_AUTH_KEYS 1
    #define NUM_CREDS 8
    #define NUM_PROVERS 1
    #define NUM_GEN 2
  #else
    #define NUM_ISSUERS 3
    #ifdef TEST_PATRAS_MODE
      #define MAX_NUMBER_OF_BLOBS 1
    #else
      #define MAX_NUMBER_OF_BLOBS 40 
    #endif
    #define NUM_GROUPS ((NUM_ISSUERS)+1)
    #define NUM_COUNTERS (NUM_ISSUERS)
    #define NUM_AUTH_KEYS 2 //((NUM_ISSUERS)+1)
    #define NUM_CREDS 3
    #define NUM_PROVERS 1
    #define NUM_GEN 2
  #endif
#endif

#define GROUP_ID_SIZE 1
#define GEN_ID_SIZE 1
#define COUNTER_ID_SIZE 1
#define COMPTYPE_SIZE 1
#define CURSOR_SIZE 4
#define PROOFSESSION_SIZE 16
#define MAX_APDU_INPUT_DATA_SIZE BUFFER_MAX_SIZE // set to 512 bytes, could possibly be 1088-2*MAX_BIGINT_SIZE
#define MAX_APDU_OUTPUT_DATA_SIZE 255 // A multos card cannot output more than 255 data bytes. We use the GET RESPONSE trick to simulate long outputs.
#define MAX_URI_SIZE 64
#define MAX_BLOB_SIZE BUFFER_MAX_SIZE

#define MAX_PIN_TRIALS 3
#define MAX_PUK_TRIALS 3
#define MIN_X_SIZE 16
#define MAX_X_SIZE 32
#define MAX_SMALLINT_SIZE 128
#define MAX_BIGINT_SIZE   256 /* Must be equal to (BUFFER_MAX_SIZE / 2), otherwise singleOrDoubleExpo and crxModularMultiplication will not work correctly */
#define HASH_SIZE 32 // byte ouput size of SHA256

/************************************************************************
 * Various structures for ABC4Trust specific types
 ************************************************************************/

typedef struct
{
  BYTE group_id;
  BYTE modulus[MAX_BIGINT_SIZE];
  unsigned int modulus_size;
  BYTE q[MAX_SMALLINT_SIZE];          // group order, do not change the size for something larger than MAX_SMALLINT_SIZE
  unsigned int q_size;
  BYTE f[MAX_BIGINT_SIZE];          // cofactor
  unsigned int f_size;
  BYTE g[NUM_GEN][MAX_BIGINT_SIZE]; // generators
  unsigned int g_size[NUM_GEN];
  BYTE num_generators;           // number of generators
} GROUP;

typedef struct
{
  BYTE counter_id;
  BYTE key_id;
  BYTE index;
  BYTE threshold;
  BYTE cursor[CURSOR_SIZE];
  BYTE exists;
} COUNTER;

typedef struct
{
  BYTE issuer_id;
  BYTE group_id;
  BYTE gen_id_1;
  BYTE gen_id_2;
  BYTE numpres;
  BYTE counter_id;
  BYTE exists;
} ISSUER;

typedef struct
{
  BYTE prover_id;
  unsigned int ksize;
  unsigned int csize;
  BYTE kx[MAX_SMALLINT_SIZE];
  BYTE c[HASH_SIZE];
  BYTE proofsession[PROOFSESSION_SIZE];
  BYTE proofstatus;
  BYTE cred_ids[NUM_CREDS];
  BYTE cred_ids_size; // also called 't' in the documentation
  BYTE exists;
} PROVER;

typedef struct
{
  BYTE credential_id;
  BYTE issuer_id;
  BYTE v[MAX_SMALLINT_SIZE]; // private key
  BYTE kv[MAX_SMALLINT_SIZE];
  BYTE status;
  BYTE prescount;
  unsigned int v_size;
  unsigned int kv_size;
  BYTE exists;
} CREDENTIAL; /* do NOT change the order of the structure elements */

typedef struct
{
  BYTE exists;
  BYTE uri[MAX_URI_SIZE];
  BYTE uri_size;
  WORD buffer_size;
} BLOB_CATALOG_ITEM;

typedef struct 
{
  BYTE buffer[MAX_BLOB_SIZE];
} BLOB_STORE_ITEM;

/************************************************************************
 * APDU structures 
 ************************************************************************/

typedef struct
{
  BYTE pin[4];
  BYTE keyId;
} APDU_READ_AUTHENTICATION_KEY;

typedef struct
{
  BYTE group_id;
  BYTE comptype;
} APDU_SET_GROUP_COMPONENT;

typedef struct
{
  BYTE group_id;
  BYTE genId;
} APDU_SET_GENERATOR;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE group_id;
} APDU_READ_GROUP_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE group_id;
  BYTE comptype;
} APDU_READ_GROUP_COMPONENT_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE group_id;
  BYTE gen_id;
} APDU_READ_GENERATOR_IN;

typedef struct
{
  BYTE counter_id;
  BYTE key_id;
  BYTE index;
  BYTE threshold;
  BYTE cursor[CURSOR_SIZE];
} APDU_SET_COUNTER;

typedef struct
{
  BYTE key_id;
  BYTE sig[MAX_BIGINT_SIZE];
} APDU_INCREMENT_COUNTER;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE counter_id;
} APDU_READ_COUNTER_IN;

typedef struct
{
  BYTE issuer_id;
  BYTE group_id;
  BYTE gen_id_1;
  BYTE gen_id_2;
  BYTE numpres;
  BYTE counter_id;
} APDU_SET_ISSUER_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE issuer_id;
} APDU_READ_ISSUER_IN;

typedef struct
{
  BYTE prover_id;
  unsigned int ksize;
  unsigned int csize;
  BYTE cred_ids[NUM_CREDS];
} APDU_SET_PROVER_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE prover_id;
} APDU_READ_PROVER_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE prover_id;
} APDU_START_COMMITMENTS_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE prover_id;
  BYTE input[MAX_APDU_INPUT_DATA_SIZE-PIN_SIZE-1];
} APDU_START_RESPONSES_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE credential_id;
  BYTE issuer_id;
} APDU_SET_CREDENTIAL_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE credential_id;
} APDU_PIN_AND_CREDENTIAL_ID;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE scope[MAX_APDU_INPUT_DATA_SIZE-PIN_SIZE];
} APDU_GET_SCOPE_EXCLUSIVE_PSEUDONYM_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE scope[MAX_APDU_INPUT_DATA_SIZE-PIN_SIZE];
} APDU_GET_SCOPE_EXCLUSIVE_COMMITMENT_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE scope[MAX_APDU_INPUT_DATA_SIZE-PIN_SIZE];
} APDU_GET_SCOPE_EXCLUSIVE_RESPONSE_IN;

typedef struct
{
  BYTE datain[PIN_SIZE+MAX_URI_SIZE];
} APDU_BLOB_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE nread;
} APDU_LIST_BLOBS_IN;

typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE password[PASSWORD_SIZE];  
} APDU_BACKUP_IN;

#ifdef SODER
typedef struct
{
  BYTE pin[PIN_SIZE];
  BYTE password[PASSWORD_SIZE];  
  BYTE credential_id;
} APDU_BACKUP_CREDENTIAL_IN;
#endif

/************************************************************************
 * Session data for the application (in RAM, but not public. Together
 * with the stack, limited to 960 bytes on ML2 cards)
 ************************************************************************/

#pragma melsession

unsigned int temp_size; // not used in subroutines

unsigned int challenge_size = 0;

unsigned int pad_size;
BYTE authKeyId;
BYTE temp_group_id;
BYTE temp_gen_id;
BYTE temp_key_id;
BYTE temp_counter_id;
BYTE temp_issuer_id;
BYTE temp_prover_id;
BYTE temp_credential_id;
BYTE temp_gen_id_1;
BYTE temp_gen_id_2;
unsigned int temp_gen_1_size;
unsigned int temp_gen_2_size;
BYTE temp_status;
BYTE *temp_modulus;
unsigned int temp_modulus_size;
BYTE d;
BYTE exit;
unsigned int temp_blob_index;
unsigned int temp_blobcount;
unsigned int temp_uri_index;
BYTE temp_nread;
BYTE prev_nread;

unsigned int temp_buffer_size;
WORD temp_rand_size;

BYTE *uri;

unsigned int i, j;

BYTE device_id_prim[ID_SIZE];
unsigned int temp_key_size;

// the following variables are used in GET RESPONSE and allow to output more than 255 bytes
WORD remaining_size;
BYTE *remaining_position;

BYTE *buffer_ptr;

union
{
  BYTE small_buffer[SMALL_BUFFER_MAX_SIZE];
  BYTE pad[MAX_BIGINT_SIZE-32];
  BYTE challenge[CHALLENGE_MAX_SIZE];
} mem_session;

/************************************************************************
 * Public data for the application (first data is placed at PB[0])
 ************************************************************************/

#pragma melpublic

BYTE temp_buffer[2*MAX_BIGINT_SIZE]; // size max can be reached in the singleResponse subroutine, cannot be less than 512 bytes

union
{
  // structures and variables used for output
  BYTE mode;
  BYTE pin_trials;
  BYTE puk_trials;
  BYTE challenge[CHALLENGE_MAX_SIZE];
  BYTE device_id[ID_SIZE];
  BYTE version[64];
  BYTE memspace[2];
  BYTE dataout[MAX_APDU_OUTPUT_DATA_SIZE];
  BYTE access_code[ACCESS_CODE_SIZE];
  BYTE mac[MAC_SIZE];
  APDU_READ_AUTHENTICATION_KEY read_authentication_key;
  APDU_SET_GROUP_COMPONENT set_group_component;
  APDU_SET_GENERATOR set_generator;
  BYTE proofsession[PROOFSESSION_SIZE];
  // structures and variables used for input
  BYTE challenge_size;
  BYTE old_pin_and_new_pin[PIN_SIZE << 1];
  BYTE puk_and_pin[PUK_SIZE + PIN_SIZE];
  BYTE id_and_size[ID_SIZE + SIZE_SIZE];
  BYTE small_buffer[SMALL_BUFFER_MAX_SIZE];
  BYTE pin[PIN_SIZE];
  BYTE buffer[BUFFER_MAX_SIZE];
  BYTE keyId;
  BYTE auth_key[MAX_BIGINT_SIZE];
  APDU_READ_GROUP_IN read_group_in;
  APDU_READ_GROUP_COMPONENT_IN read_group_component_in;
  APDU_READ_GENERATOR_IN read_generator_in;
  BYTE group_id;
  APDU_SET_COUNTER set_counter;
  APDU_INCREMENT_COUNTER increment_counter;
  APDU_READ_COUNTER_IN read_counter_in;
  BYTE counter_id;
  APDU_SET_ISSUER_IN set_issuer_in;
  APDU_READ_ISSUER_IN read_issuer_in;
  BYTE issuer_id;
  APDU_SET_PROVER_IN set_prover_in;
  APDU_READ_PROVER_IN read_prover_in;
  BYTE prover_id;
  APDU_START_COMMITMENTS_IN start_commitments_in;
  APDU_START_RESPONSES_IN start_responses_in;
  APDU_SET_CREDENTIAL_IN set_credential_in;
  APDU_PIN_AND_CREDENTIAL_ID pin_and_credential_id;
  APDU_GET_SCOPE_EXCLUSIVE_PSEUDONYM_IN get_scope_exclusive_pseudonym_in;
  APDU_GET_SCOPE_EXCLUSIVE_COMMITMENT_IN get_scope_exclusive_commitment_in;
  APDU_GET_SCOPE_EXCLUSIVE_RESPONSE_IN get_scope_exclusive_response_in;
  APDU_BLOB_IN blob_in;
  APDU_LIST_BLOBS_IN list_blobs_in;
  APDU_BACKUP_IN backup_in;
  #ifdef SODER
  APDU_BACKUP_CREDENTIAL_IN backup_credential_in;
  #endif
} apdu_data;



/************************************************************************
 * Static data for the application
 ************************************************************************/

#pragma melstatic

BLOB_STORE_ITEM   blob_store[MAX_NUMBER_OF_BLOBS]; // do not declare anything above 'blob_store': anything declared above blobs could be located in higher (not directly accessible) part of the e2 memory.
BLOB_CATALOG_ITEM blob_catalog[MAX_NUMBER_OF_BLOBS];

/**
 * Constant definition for test mode only
 * Constraints in test mode:
 *
 * - During "INITIALIZE DEVICE", device id should be 0xacac and size
 * should be 16 (0x10)
 *
 * In general, when a random string is drawn in test mode, all the
 * bytes are set to 0xbc.
 */

#ifdef TEST_MODE

BYTE test_device_key[16] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
BYTE test_pin[4] = {0x00, 0x00, 0x00, 0x00};
BYTE test_puk[8] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

#endif

static char version[64] = "ABC4Trust Card Lite - CryptoExperts Jan 2013 - Swedish Pilot v1 "; // 64 bytes

BYTE master_backup_key[MASTER_BACKUP_KEY_SIZE] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

#ifdef TEST_MODE
BYTE root_code[ACCESS_CODE_SIZE] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
#else
BYTE root_code[ACCESS_CODE_SIZE] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
#endif

BYTE resurrection_key[RESURRECTION_KEY_SIZE] = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

BYTE pin_trials = MAX_PIN_TRIALS;
BYTE puk_trials = MAX_PUK_TRIALS;
BYTE device_id[ID_SIZE];
unsigned int x_size;
BYTE device_key[MAX_SMALLINT_SIZE];
BYTE puk[PUK_SIZE];
BYTE pin[PIN_SIZE];
BYTE mode = MODE_VIRGIN;
BYTE auth_keys[NUM_AUTH_KEYS][MAX_BIGINT_SIZE];
unsigned int auth_keys_sizes[NUM_AUTH_KEYS]; // auth_keys_exist[key_id] > 0 iff the key exists, 0 otherwise
BYTE buffer[BUFFER_MAX_SIZE];
unsigned int buffer_size = 0;
BYTE authData = 0;
GROUP groups[NUM_GROUPS];
#if NUM_COUNTERS > 0
COUNTER counters[NUM_COUNTERS];
#endif
ISSUER issuers[NUM_ISSUERS];
PROVER provers[NUM_PROVERS];
BYTE current_prover_id;
CREDENTIAL credentials[NUM_CREDS];

BYTE temp_key[MAX_BIGINT_SIZE];

/*******************************************************************************************************************************************************/
/*******************************************************************************************************************************************************/
/*************************************************             void main(void)            **************************************************************/
/*******************************************************************************************************************************************************/
/*******************************************************************************************************************************************************/

void main(void) 
{

  /* Check class in APDU. */
  if (CLA != ABC_CLA && INS != INS_GET_RESPONSE)
    exitSW(ERR_BAD_CLA);
  
  /* Decode instruction. */
  switch (INS)
    {

#ifdef TEST_PATRAS_MODE

      /***************************
       * INS_GET_KX_AND_DEVICE_KEY
       ***************************/
      
    case INS_GET_KX_AND_DEVICE_KEY:

      if (!CheckCase(2)) 
	exitSW(ERR_BAD_ISO);

      if (current_prover_id < 1 || current_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);
      
      temp_size = 0;

      memcpy(temp_buffer+temp_size, &(provers[current_prover_id-1].ksize), 2);
      temp_size += 2;

      memcpy(temp_buffer+temp_size, provers[current_prover_id-1].kx + MAX_SMALLINT_SIZE - provers[current_prover_id-1].ksize, provers[current_prover_id-1].ksize);
      temp_size += provers[current_prover_id-1].ksize;

      memcpy(temp_buffer+temp_size, &x_size, 2);
      temp_size += 2;

      memcpy(temp_buffer+temp_size, device_key+MAX_SMALLINT_SIZE-x_size, x_size);
      temp_size += x_size;
      
      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /**********
       * INS_TEST
       **********/
      
    case INS_TEST:

      // do whatever you like here...

      break;

#endif

      /************
       * GET MODE 
       ************/
      
    case INS_GET_MODE:

      if (!CheckCase(2)) 
	exitSW(ERR_BAD_ISO);

      apdu_data.mode = mode;

      exitLa(1);

      break;

      /***************
       * SET ROOT MODE
       ***************/

    case INS_SET_ROOT_MODE:
      if (!CheckCase(3)) 
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_VIRGIN)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != ACCESS_CODE_SIZE)
	exitSW((ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ ACCESS_CODE_SIZE));

      if (memcmp(apdu_data.access_code, root_code, ACCESS_CODE_SIZE) != 0)
	exitSW(ERR_FAILED_ACCESS_PRIVILEGED_MODE);

      mode = MODE_ROOT;

      break;

      /******************
       * SET WORKING MODE
       ******************/

    case INS_SET_WORKING_MODE:
      if (!CheckCase(1)) 
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT)
	exitSW(ERR_BAD_MODE ^ mode);

      mode = MODE_WORKING;

      break;

      /*****************
       * SET VIRGIN MODE
       *****************/
#ifndef TEST_MODE
    case INS_SET_VIRGIN_MODE:
      if (!CheckCase(3)) 
	exitSW(ERR_BAD_ISO);
      
      if (Lc != MAC_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ MAC_SIZE);
      
      if (challenge_size < 16)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_RAND ^ 16);

      memcpy(temp_buffer, resurrection_key, RESURRECTION_KEY_SIZE);
      memcpy(temp_buffer+RESURRECTION_KEY_SIZE, mem_session.challenge, challenge_size);
      buffer_ptr = temp_buffer + RESURRECTION_KEY_SIZE + challenge_size;
      SHA256(buffer_ptr, RESURRECTION_KEY_SIZE + challenge_size, temp_buffer);

      memset(mem_session.challenge, 0, CHALLENGE_MAX_SIZE);
      challenge_size = 0;
      
      if (memcmp(apdu_data.mac, buffer_ptr, MAC_SIZE) != 0)
	exitSW(ERR_FAILED_ACCESS_PRIVILEGED_MODE);

      // Erase the entire contents of the static memory and reset some of the variables to their original value

      pin_trials = MAX_PIN_TRIALS;
      puk_trials = MAX_PUK_TRIALS;
      memset(device_id, 0, ID_SIZE);
      x_size = 0;
      memset(device_key, 0, MAX_SMALLINT_SIZE);
      memset(puk, 0, PUK_SIZE);
      memset(pin, 0, PIN_SIZE);
      memset(auth_keys, 0, NUM_AUTH_KEYS*MAX_BIGINT_SIZE);
      memset(auth_keys_sizes, 0, sizeof(WORD)*NUM_AUTH_KEYS);
      memset(buffer, 0, BUFFER_MAX_SIZE);
      buffer_size = 0;
      authData = 0;
      memset(groups, 0, sizeof(GROUP)*NUM_GROUPS);
      #if NUM_COUNTERS > 0
      memset(counters, 0, sizeof(COUNTER)*NUM_COUNTERS);
      #endif
      memset(issuers, 0, sizeof(ISSUER)*NUM_ISSUERS);
      memset(provers, 0, sizeof(PROVER)*NUM_PROVERS);
      current_prover_id = 0;
      memset(credentials, 0, sizeof(CREDENTIAL)*NUM_CREDS);
      //setStaticHigh(blob_store, 0x00, sizeof(BLOB_STORE_ITEM)*MAX_NUMBER_OF_BLOBS); // TODO !!!
      memset(blob_catalog, 0x00, sizeof(BLOB_CATALOG_ITEM)*MAX_NUMBER_OF_BLOBS);
      memset(temp_key, 0, MAX_BIGINT_SIZE);

      mode = MODE_VIRGIN;

      break;

      /*****************************
       * SET VIRGIN MODE (TEST MODE)
       *****************************/
#else
    case INS_SET_VIRGIN_MODE:
      if (!CheckCase(3)) 
	exitSW(ERR_BAD_ISO);
      
      if (Lc != MAC_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ MAC_SIZE);
      
      if (challenge_size < 16)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_RAND ^ 16);

      memcpy(temp_buffer, resurrection_key, RESURRECTION_KEY_SIZE);
      memcpy(temp_buffer+RESURRECTION_KEY_SIZE, mem_session.challenge, challenge_size);
      buffer_ptr = temp_buffer + RESURRECTION_KEY_SIZE + challenge_size;
      SHA256(buffer_ptr, RESURRECTION_KEY_SIZE + challenge_size, temp_buffer);

      memset(mem_session.challenge, 0, CHALLENGE_MAX_SIZE);
      challenge_size = 0;
      
      if (memcmp(apdu_data.mac, buffer_ptr, MAC_SIZE) != 0)
	exitSW(ERR_FAILED_ACCESS_PRIVILEGED_MODE);

      // Erase the entire contents of the static memory and reset some of the variables to their original value

      pin_trials = MAX_PIN_TRIALS;
      puk_trials = MAX_PUK_TRIALS;
      memset(device_id, 0, ID_SIZE);
      x_size = 0;
      memset(device_key, 0, MAX_SMALLINT_SIZE);
      memset(puk, 0, PUK_SIZE);
      memset(pin, 0, PIN_SIZE);
      memset(auth_keys, 0, NUM_AUTH_KEYS*MAX_BIGINT_SIZE);
      memset(auth_keys_sizes, 0, sizeof(WORD)*NUM_AUTH_KEYS);
      memset(buffer, 0, BUFFER_MAX_SIZE);
      buffer_size = 0;
      authData = 0;
      for (i = 0; i < NUM_GROUPS; i++) {
	groups[i].modulus_size = 0;
	groups[i].q_size = 0;
	groups[i].f_size = 0;
	for (j = 0; j < NUM_GEN; j++) {
	  groups[i].g_size[j] = 0;
	}
	groups[i].num_generators = 0;
      }
      #if NUM_COUNTERS > 0
      memset(counters, 0, sizeof(COUNTER)*NUM_COUNTERS);
      # endif
      memset(issuers, 0, sizeof(ISSUER)*NUM_ISSUERS);
      for (i = 0; i < NUM_PROVERS; i++) {
	provers[i].ksize = 0;
	provers[i].csize = 0;
	provers[i].proofstatus = 0;
	provers[i].cred_ids_size = 0;
	provers[i].exists = 0;
      }
      for (i = 0; i < NUM_CREDS; i++) {
	credentials[i].status = 0;
	credentials[i].prescount = 0;
	credentials[i].v_size = 0;
	credentials[i].kv_size = 0;
	credentials[i].exists = 0;
      }
      for (i = 0; i < MAX_NUMBER_OF_BLOBS; i++) {
	blob_catalog[i].exists = 0;
	blob_catalog[i].uri_size = 0;
	blob_catalog[i].buffer_size = 0;
      }

      current_prover_id = 0;

      mode = MODE_VIRGIN;

      break;
#endif
      /*****************
       * PIN TRIALS LEFT
       *****************/

    case INS_PIN_TRIALS_LEFT:
      if (!CheckCase(2))
	exitSW(ERR_BAD_ISO);

      if (mode == MODE_VIRGIN)
	exitSW(ERR_BAD_MODE);

      apdu_data.pin_trials = pin_trials;
      exitLa(1);

      break;

      /*****************
       * PUK TRIALS LEFT
       *****************/

    case INS_PUK_TRIALS_LEFT:
      if (!CheckCase(2))
	exitSW(ERR_BAD_ISO);

      if (mode == MODE_VIRGIN)
	exitSW(ERR_BAD_MODE);

      apdu_data.puk_trials = puk_trials;
      exitLa(1);

      break;

      /************
       * CHANGE PIN
       ************/
      
    case INS_CHANGE_PIN:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != (PIN_SIZE<<1))
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE<<1));
      
      checkPin(apdu_data.old_pin_and_new_pin);
      
      memcpy(pin, apdu_data.old_pin_and_new_pin + PIN_SIZE, PIN_SIZE);

      pin_trials = MAX_PIN_TRIALS;

      break;

      /***********
       * RESET PIN
       ***********/

    case INS_RESET_PIN:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING && mode != MODE_LOCKED)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + PUK_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PUK_SIZE));
      
      checkPuk(apdu_data.puk_and_pin);
      
      memcpy(pin, apdu_data.puk_and_pin + PUK_SIZE, PIN_SIZE);
      
      pin_trials = MAX_PIN_TRIALS;

      mode = MODE_WORKING;
      
      break;

      /*******************
       * INITIALIZE DEVICE
        *******************/

    case INS_INITIALIZE_DEVICE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != ID_SIZE + SIZE_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (ID_SIZE + SIZE_SIZE));

      memcpy(device_id, apdu_data.id_and_size, ID_SIZE);

      temp_size = sizeDecode(apdu_data.id_and_size + ID_SIZE);

      if (temp_size < MIN_X_SIZE)
	exitSW(ERR_DEVICE_KEY_SHORTER_THAN_MIN_X_SIZE);

      if (temp_size > MAX_SMALLINT_SIZE)
	exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);

      x_size = temp_size;

#ifdef TEST_MODE
      memcpy(device_key+MAX_SMALLINT_SIZE-temp_size, test_device_key, temp_size);
      memcpy(pin, test_pin, PIN_SIZE);
      memcpy(puk, test_puk, PUK_SIZE);
#else
      getRandomBytes(device_key+MAX_SMALLINT_SIZE-temp_size, temp_size);
      getRandomBytes(temp_buffer, PIN_SIZE+PUK_SIZE);
      for (i = 0; i < PUK_SIZE; i++) {
	puk[i] = 0x30 + (temp_buffer[i] % 10);
      }
      for (i = 0; i < PIN_SIZE; i++) {
	pin[i] = 0x30 + (temp_buffer[i+PUK_SIZE] % 10);
      }
#endif

      getKey(temp_key, &temp_key_size, 0); 

      memcpy(temp_buffer         , pin, PIN_SIZE);
      memcpy(temp_buffer+PIN_SIZE, puk, PUK_SIZE); 
      temp_buffer_size = PIN_SIZE + PUK_SIZE; 

      publicKeyEncrypt(temp_key, temp_key_size); 

      memset(&temp_key, 0, MAX_BIGINT_SIZE);

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size); 
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /***************
       * GET DEVICE ID
       **************/
      
    case INS_GET_DEVICE_ID:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ PIN_SIZE);
      
      checkPin(apdu_data.pin);
      
      memcpy(apdu_data.device_id, device_id, ID_SIZE);

      exitLa(ID_SIZE);

      break;

      /*************
       * GET VERSION
       *************/

    case INS_GET_VERSION:
      if (!CheckCase(2))
	exitSW(ERR_BAD_ISO);

      memcpy(apdu_data.version, version, 64);

      exitLa(64);

      break;

      /**********
       * PUT DATA
       **********/
      
    case INS_PUT_DATA:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc == 0)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA ^ 0x01);
      
      if (Lc > BUFFER_MAX_SIZE)
	buffer_size = BUFFER_MAX_SIZE;
      else
	buffer_size = Lc;

      memcpy(buffer, apdu_data.buffer, buffer_size);

      break;

      /***************
       * GET CHALLENGE
       ***************/

    case INS_GET_CHALLENGE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (Lc != 1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ 1);

      challenge_size = (unsigned int)apdu_data.challenge_size;
      
      if (!challenge_size)
	challenge_size = CHALLENGE_MAX_SIZE;

#ifdef TEST_MODE
      memset(mem_session.challenge, 0xaa, challenge_size);
#else
      getRandomBytes(mem_session.challenge, challenge_size);
#endif

      if (challenge_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.challenge, mem_session.challenge, challenge_size);
	exitLa(challenge_size);
      } else {
	remaining_position = mem_session.challenge;
	remaining_size = challenge_size;
	output_large_data();
      }

      break;


      /*******************
       * AUTHENTICATE DATA
       *******************/

    case INS_AUTHENTICATE_DATA:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != 1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ 1);

      getKey(temp_key, &temp_key_size, apdu_data.keyId);

      authData = 0;

      extract(temp_key, temp_key_size);

      authData = 1;

      authKeyId = apdu_data.keyId;

      memset(&temp_key, 0, MAX_BIGINT_SIZE);
      
      break;

      /************************
       * SET AUTHENTICATION KEY
       ************************/

    case INS_SET_AUTHENTICATION_KEY:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != 1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ 1);

      if (apdu_data.keyId >= NUM_AUTH_KEYS)
	exitSW(ERR_KEY_ID_OUTSIDE_RANGE);

      if (mode == MODE_WORKING)
	checkBufferPrefix(INS_SET_AUTHENTICATION_KEY, &apdu_data.keyId, 1);

      if (buffer_size < 55)
	exitSW(ERR_AUTHENTICATION_KEY_TOO_SHORT);

      if (buffer_size > MAX_BIGINT_SIZE)
	exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);

      memcpy(auth_keys[apdu_data.keyId], buffer, buffer_size);
      auth_keys_sizes[apdu_data.keyId] = buffer_size;
      
      buffer_size = 0;

      break;

      /**************************
       * LIST AUTHENTICATION KEYS
       **************************/

    case INS_LIST_AUTHENTICATION_KEYS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ PIN_SIZE);
      
      checkPin(apdu_data.pin);

      temp_size = 0; // size of temp_buffer
      for (authKeyId=0; authKeyId < NUM_ISSUERS; authKeyId++) {
	if (auth_keys_sizes[authKeyId]) {
	  memcpy(temp_buffer + temp_size, &authKeyId, 1);
	  memcpy(temp_buffer + temp_size + 1, &(auth_keys_sizes[authKeyId]), 2);
	  temp_size = temp_size + 3;
	}
      }
      
      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /*************************
       * READ AUTHENTICATION KEY
       *************************/

    case INS_READ_AUTHENTICATION_KEY:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + KEY_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + KEY_ID_SIZE));
      
      checkPin(apdu_data.read_authentication_key.pin);

      getKey(temp_buffer, &temp_size, apdu_data.read_authentication_key.keyId);

      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.auth_key, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /***************************
       * REMOVE AUTHENTICATION KEY
       ***************************/

    case INS_REMOVE_AUTHENTICATION_KEY:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != KEY_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ KEY_ID_SIZE);
      
      if (apdu_data.keyId >= NUM_AUTH_KEYS)
	exitSW(ERR_KEY_ID_OUTSIDE_RANGE);

      if (!auth_keys_sizes[apdu_data.keyId])
	exitSW(ERR_AUTHENTICATION_KEY_DOES_NOT_EXIST);
      
      if (mode == MODE_WORKING)
	checkBufferEqual(INS_REMOVE_AUTHENTICATION_KEY, &(apdu_data.keyId), KEY_ID_SIZE);

      memset(auth_keys[apdu_data.keyId], 0x00, auth_keys_sizes[apdu_data.keyId]);
      auth_keys_sizes[apdu_data.keyId] = 0;

      break;

      /*********************
       * SET GROUP COMPONENT
       *********************/

    case INS_SET_GROUP_COMPONENT:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != GROUP_ID_SIZE + COMPTYPE_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (GROUP_ID_SIZE + COMPTYPE_SIZE));
      
      if (apdu_data.set_group_component.group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (apdu_data.set_group_component.comptype > 2)
	exitSW(ERR_COMPONENT_TYPE_OUTSIDE_OF_RANGE);

      if (mode == MODE_WORKING)
	checkBufferPrefix(INS_SET_GROUP_COMPONENT, &apdu_data.set_group_component.group_id, 2); // trick to get group_id || comptype

      if (buffer_size > MAX_BIGINT_SIZE)
	exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);

      switch(apdu_data.set_group_component.comptype) {
      case 0:
	memset(groups[apdu_data.set_group_component.group_id].modulus, 0, MAX_BIGINT_SIZE-buffer_size);
	memcpy(groups[apdu_data.set_group_component.group_id].modulus+(MAX_BIGINT_SIZE-buffer_size), buffer, buffer_size); // put the buffer data on the right-most bytes of modulus
	groups[apdu_data.set_group_component.group_id].modulus_size = buffer_size;
	break;
      case 1:
	if (buffer_size > MAX_SMALLINT_SIZE)
	  exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);
	memcpy(groups[apdu_data.set_group_component.group_id].q+(MAX_SMALLINT_SIZE-buffer_size), buffer, buffer_size);
	groups[apdu_data.set_group_component.group_id].q_size = buffer_size;
	break;	
      case 2:
	memcpy(groups[apdu_data.set_group_component.group_id].f+(MAX_BIGINT_SIZE-buffer_size), buffer, buffer_size);
	groups[apdu_data.set_group_component.group_id].f_size = buffer_size;
	break;	
      default:
	break;
      }

      break;

      /***************
       * SET GENERATOR
       ***************/

    case INS_SET_GENERATOR:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != GROUP_ID_SIZE + GEN_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (GROUP_ID_SIZE + GEN_ID_SIZE));

      temp_group_id = apdu_data.set_generator.group_id;
      temp_gen_id   = apdu_data.set_generator.genId;

      if (temp_group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (temp_gen_id == 0 || temp_gen_id > NUM_GEN)
	exitSW(ERR_ID_OF_GROUP_GENERATOR_OUTSIDE_OF_RANGE);

      if (mode == 2)
	checkBufferPrefix(INS_SET_GENERATOR, &apdu_data.set_generator.group_id, 2); // trick to get group_id || genId

      if (buffer_size > MAX_BIGINT_SIZE)
	exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);

      memcpy(groups[temp_group_id].g[temp_gen_id-1]+(MAX_BIGINT_SIZE-buffer_size), buffer, buffer_size); // align the buffer data to the right-most bytes of g[]
      if (groups[temp_group_id].g_size[temp_gen_id-1] == 0) {
	groups[temp_group_id].num_generators += 1;
      }
      groups[temp_group_id].g_size[temp_gen_id-1] = buffer_size;

      break;


      /*************
       * LIST GROUPS
       *************/

    case INS_LIST_GROUPS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      temp_size = 0; // size of dataout
      for (temp_group_id = 0; temp_group_id < NUM_GROUPS; temp_group_id++)
	if (groupExists(temp_group_id)) {
	  memcpy(temp_buffer + temp_size, &temp_group_id, 1);
	  temp_size++;
	}
	 
      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /************
       * READ GROUP
       ************/

    case INS_READ_GROUP:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + GROUP_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + GROUP_ID_SIZE));

      checkPin(apdu_data.read_group_in.pin);

      temp_group_id = apdu_data.read_group_in.group_id;

      if (temp_group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (!groupExists(temp_group_id))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      temp_size = 0;

      getGroupComponent(temp_group_id, 0x00);
      memcpy(temp_buffer+temp_size, &buffer_size, SIZE_SIZE);
      temp_size += SIZE_SIZE;
      
      getGroupComponent(temp_group_id, 0x01);
      memcpy(temp_buffer+temp_size, &buffer_size, SIZE_SIZE);
      temp_size += SIZE_SIZE;

      getGroupComponent(temp_group_id, 0x02);
      memcpy(temp_buffer+temp_size, &buffer_size, SIZE_SIZE);
      temp_size += SIZE_SIZE;

      getGroupComponent(temp_group_id, 0x03);
      memcpy(temp_buffer+temp_size, buffer, buffer_size);
      temp_size += 1;

      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /**********************
       * READ GROUP COMPONENT
       **********************/

    case INS_READ_GROUP_COMPONENT:

      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + GROUP_ID_SIZE + COMPTYPE_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + GROUP_ID_SIZE + COMPTYPE_SIZE));

      checkPin(apdu_data.read_group_component_in.pin);

      if (apdu_data.read_group_component_in.group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (!groupExists(apdu_data.read_group_component_in.group_id))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      if (apdu_data.read_group_component_in.comptype > 3)
	exitSW(ERR_COMPONENT_TYPE_OUTSIDE_OF_RANGE);

      getGroupComponent(apdu_data.read_group_component_in.group_id, apdu_data.read_group_component_in.comptype);

      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer;
	remaining_size = buffer_size;
	output_large_data();
      }


      break;

      /****************
       * READ GENERATOR
       ****************/

    case INS_READ_GENERATOR:

      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + GROUP_ID_SIZE + GEN_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + GROUP_ID_SIZE + GEN_ID_SIZE));

      checkPin(apdu_data.read_generator_in.pin);

      if (apdu_data.read_generator_in.group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (!groupExists(apdu_data.read_generator_in.group_id))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      getGenerator(apdu_data.read_generator_in.group_id, apdu_data.read_generator_in.gen_id); // temp_size holds the true size of the generator
      temp_size = buffer_size;
      
      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer+MAX_BIGINT_SIZE-temp_size, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = buffer+MAX_BIGINT_SIZE-temp_size;
	remaining_size = temp_size;
	output_large_data();
      }


      break;

      /**************
       * REMOVE GROUP
       **************/
      
    case INS_REMOVE_GROUP:
      
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != GROUP_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ GROUP_ID_SIZE);

      if (apdu_data.group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (!groupExists(apdu_data.group_id))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      if (mode == MODE_WORKING)
	checkBufferPrefix(INS_REMOVE_GROUP, &apdu_data.group_id, GROUP_ID_SIZE);

      groups[apdu_data.group_id].modulus_size = 0;
      groups[apdu_data.group_id].q_size = 0;
      groups[apdu_data.group_id].f_size = 0;
      for (i=0; i<NUM_GEN; i++)
	groups[apdu_data.group_id].g_size[i] = 0;
      groups[apdu_data.group_id].num_generators = 0;

      break;
      
      
      /*************
       * SET COUNTER
       *************/

    #if NUM_COUNTERS > 0
    case INS_SET_COUNTER:

      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != 4 + CURSOR_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (4 + CURSOR_SIZE));

      if (apdu_data.set_counter.counter_id < 1 || apdu_data.set_counter.counter_id > NUM_COUNTERS)
	exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);

      if (apdu_data.set_counter.key_id > NUM_ISSUERS)
	exitSW(ERR_KEY_ID_OUTSIDE_RANGE);

      if (mode == MODE_WORKING)
	checkBufferEqual(INS_SET_COUNTER, &(apdu_data.set_counter.counter_id), 4+CURSOR_SIZE); // trick to get counter_id||key_id||...

      temp_counter_id = apdu_data.set_counter.counter_id;

      memcpy(&(counters[temp_counter_id-1]), &(apdu_data.set_counter.counter_id), 4 + CURSOR_SIZE); // this sets counter_id, key_id, index, threshold,cursor, all at once
      counters[temp_counter_id-1].exists = 1;

      break;
    #endif


      /*******************
       * INCREMENT COUNTER
       *******************/
    
    #if NUM_COUNTERS > 0
    case INS_INCREMENT_COUNTER:

      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < 56)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA ^ 0x38);

      temp_key_id = apdu_data.increment_counter.key_id;

      memcpy(buffer, apdu_data.increment_counter.sig, Lc-1);
      buffer_size = Lc-1;

      getKey(temp_key, &temp_key_size, temp_key_id);

      extract(temp_key, temp_key_size);

      if (buffer_size != 5)
	exitSW(ERR_DATA_AUTHENTICATION_FAILURE);

      temp_counter_id = buffer[0];
      memcpy(mem_session.small_buffer, buffer+1, CURSOR_SIZE);

      if (temp_counter_id < 1 || temp_counter_id > NUM_COUNTERS)
	exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);

      if (!counters[temp_counter_id-1].exists)
	exitSW(ERR_COUNTER_DOES_NOT_EXIST);

      if (temp_key_id != counters[temp_counter_id-1].key_id)
	exitSW(ERR_DATA_AUTHENTICATION_FAILURE);

      if (memcmp(mem_session.small_buffer,counters[temp_counter_id-1].cursor, CURSOR_SIZE) > 0) {
	memcpy(counters[temp_counter_id-1].cursor, mem_session.small_buffer, CURSOR_SIZE);
	counters[temp_counter_id-1].index += 1;
      }

      memset(&temp_key, 0, MAX_BIGINT_SIZE);

      break;
    #endif

      /***************
       * LIST COUNTERS
       ***************/

    #if NUM_COUNTERS > 0      
    case INS_LIST_COUNTERS:

      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      temp_size = 0;

      for (temp_counter_id = 1; temp_counter_id <= NUM_COUNTERS; temp_counter_id++) {
	if (counters[temp_counter_id-1].exists) {
	  temp_buffer[temp_size] = temp_counter_id;
	  temp_size++;
	}
      }

      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;
    #endif

      /**************
       * READ COUNTER
       **************/
    
    #if NUM_COUNTERS > 0
    case INS_READ_COUNTER:

      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + COUNTER_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + COUNTER_ID_SIZE));

      checkPin(apdu_data.read_counter_in.pin);

      temp_counter_id = apdu_data.read_counter_in.counter_id;

      if (temp_counter_id < 1 || temp_counter_id > NUM_COUNTERS)
	exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);

      if (!counters[temp_counter_id-1].exists)
	exitSW(ERR_COUNTER_DOES_NOT_EXIST);

      memcpy(apdu_data.dataout, &(counters[temp_counter_id-1].key_id), 3 + CURSOR_SIZE); // get key_id || index || ...

      exitLa(3 + CURSOR_SIZE);

      break;
    #endif

      /****************
       * REMOVE COUNTER
       ****************/

    #if NUM_COUNTERS > 0
    case INS_REMOVE_COUNTER:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != COUNTER_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ COUNTER_ID_SIZE);

      temp_counter_id = apdu_data.counter_id;
      
      if (temp_counter_id < 1 || temp_counter_id > NUM_COUNTERS)
	exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);

      if (!counters[temp_counter_id-1].exists)
	exitSW(ERR_COUNTER_DOES_NOT_EXIST);

      if (mode == MODE_WORKING)
	checkBufferEqual(INS_REMOVE_COUNTER, &temp_counter_id, COUNTER_ID_SIZE);

      memset(&(counters[temp_counter_id-1].counter_id), 0, 5+CURSOR_SIZE); // also set 'exists' to 0

      break;
    #endif

      /************
       * SET ISSUER
       ************/

    case INS_SET_ISSUER:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != 6)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ 6);

      temp_issuer_id = apdu_data.set_issuer_in.issuer_id;

      if (temp_issuer_id < 1 || temp_issuer_id > NUM_ISSUERS)
	exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);

      if (apdu_data.set_issuer_in.group_id >= NUM_GROUPS)
	exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

      if (apdu_data.set_issuer_in.gen_id_1 < 1 || apdu_data.set_issuer_in.gen_id_1 > NUM_GEN || apdu_data.set_issuer_in.gen_id_2 > NUM_GEN)
	exitSW(ERR_ID_OF_GROUP_GENERATOR_OUTSIDE_OF_RANGE);

      if (apdu_data.set_issuer_in.counter_id > NUM_COUNTERS)
	exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);
      
      if (mode == MODE_WORKING)
	checkBufferEqual(INS_SET_ISSUER, &(temp_issuer_id), 6); // get issuer_id||group_id||...

      memcpy(&(issuers[temp_issuer_id-1].issuer_id), &(apdu_data.set_issuer_in.issuer_id), 6);
      issuers[temp_issuer_id-1].exists = 1;

      break;

      /**************
       * LIST ISSUERS
       **************/

    case INS_LIST_ISSUERS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      temp_size = 0;

      for (temp_issuer_id = 1; temp_issuer_id <= NUM_ISSUERS; temp_issuer_id++)
	if (issuers[temp_issuer_id-1].exists) {
	  memcpy(temp_buffer + temp_size, &temp_issuer_id, 1);
	  temp_size++;
	}

      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }


      break;

      /*************
       * READ ISSUER
       *************/

    case INS_READ_ISSUER:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE+1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE+1));

      checkPin(apdu_data.read_issuer_in.pin);

      temp_issuer_id = apdu_data.read_issuer_in.issuer_id;

      if (temp_issuer_id < 1 || temp_issuer_id > NUM_ISSUERS)
	exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
      
      if (!issuers[temp_issuer_id-1].exists)
	exitSW(ERR_ISSUER_DOES_NOT_EXIST);

      memcpy(apdu_data.dataout, &(issuers[temp_issuer_id-1].group_id), 5);

      exitLa(5);

      break;
      
      /***************
       * REMOVE ISSUER
       ***************/

    case INS_REMOVE_ISSUER:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != 1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ 1);

      temp_issuer_id = apdu_data.issuer_id;
      
      if (temp_issuer_id < 1 || temp_issuer_id > NUM_ISSUERS)
	exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
      
      if (!issuers[temp_issuer_id-1].exists)
	exitSW(ERR_ISSUER_DOES_NOT_EXIST);

      if (mode == MODE_WORKING)
	checkBufferEqual(INS_REMOVE_ISSUER, &temp_issuer_id, 1);

      memset(&(issuers[temp_issuer_id-1]), 0, 7); // also set 'exists' to 0

      break;

      /************
       * SET PROVER
       ************/

    case INS_SET_PROVER:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < 5)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ 5);

      temp_prover_id = apdu_data.set_prover_in.prover_id;
      
      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

      for (i=0; i < Lc-5; i++)
	if (apdu_data.set_prover_in.cred_ids[i] > NUM_CREDS)
	  exitSW(ERR_ONE_ID_OF_CREDIDS_IS_OUTSIDE_OF_RANGE);

      if (mode == MODE_WORKING)
	checkBufferEqual(INS_SET_PROVER, &(apdu_data.set_prover_in.prover_id), Lc);

      memcpy(&(provers[temp_prover_id-1].prover_id), &(apdu_data.set_prover_in.prover_id), 5); // under the hood, this also initializes ksize and csize
      memset(&(provers[temp_prover_id-1].kx), 0, MAX_SMALLINT_SIZE);
      memset(&(provers[temp_prover_id-1].c), 0, HASH_SIZE);
      memset(&(provers[temp_prover_id-1].proofsession), 0, PROOFSESSION_SIZE);
      provers[temp_prover_id-1].proofstatus = 0;
      provers[temp_prover_id-1].cred_ids_size = Lc-5;
      memcpy(provers[temp_prover_id-1].cred_ids, apdu_data.set_prover_in.cred_ids, Lc-5);
      provers[temp_prover_id-1].exists = 1;

      break;

      /*************
       * READ PROVER
       *************/

    case INS_READ_PROVER:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + PROVER_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + PROVER_ID_SIZE));
      
      checkPin(apdu_data.read_prover_in.pin);

      temp_prover_id = apdu_data.read_prover_in.prover_id;

      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

      if (!provers[temp_prover_id-1].exists)
	exitSW(ERR_PROVER_DOES_NOT_EXIST);

      memcpy(temp_buffer, &(provers[temp_prover_id-1].ksize), 4); // copy ksize || csize
      memcpy(temp_buffer+4, &(provers[temp_prover_id-1].proofsession), PROOFSESSION_SIZE + 1 + provers[temp_prover_id-1].cred_ids_size); // proofsession || proofstatus || cred_ids

      temp_size = 5 + PROOFSESSION_SIZE + provers[temp_prover_id-1].cred_ids_size;

      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }


      break;

      /***************
       * REMOVE PROVER
       ***************/

    case INS_REMOVE_PROVER:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PROVER_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ PROVER_ID_SIZE);
      
      temp_prover_id = apdu_data.prover_id;

      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

      if (!provers[temp_prover_id-1].exists)
	exitSW(ERR_PROVER_DOES_NOT_EXIST);

      if (mode == 2)
	checkBufferEqual(INS_REMOVE_PROVER, &(temp_prover_id), 1);

      memset(&(provers[temp_prover_id-1]), 0, sizeof(PROVER));

      break;

      /*******************
       * START COMMITMENTS
       *******************/

    case INS_START_COMMITMENTS:

      #ifdef SODER
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      #else
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      #endif
      
      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + PROVER_ID_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + PROVER_ID_SIZE));
      
      checkPin(apdu_data.start_commitments_in.pin);

      temp_prover_id = apdu_data.start_commitments_in.prover_id;

      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

      if (!provers[temp_prover_id-1].exists)
	exitSW(ERR_PROVER_DOES_NOT_EXIST);

      memset(provers[temp_prover_id-1].kx, 0, MAX_SMALLINT_SIZE - provers[temp_prover_id-1].ksize); // put 0x00's on the left of kx

#ifdef TEST_MODE
      memset(provers[temp_prover_id-1].kx + MAX_SMALLINT_SIZE - provers[temp_prover_id-1].ksize, 0xaa, provers[temp_prover_id-1].ksize);
      memset(provers[temp_prover_id-1].proofsession, 0xaa, PROOFSESSION_SIZE);
#else
      getRandomBytes(provers[temp_prover_id-1].kx + MAX_SMALLINT_SIZE - provers[temp_prover_id-1].ksize, provers[temp_prover_id-1].ksize);
      getRandomBytes(provers[temp_prover_id-1].proofsession, PROOFSESSION_SIZE);
#endif
      provers[temp_prover_id-1].proofstatus = 1;
      
      current_prover_id = temp_prover_id;
      
      // For Patras 2, we do not use the proofsession
      
      #ifdef SODER
      memcpy(apdu_data.proofsession, provers[temp_prover_id-1].proofsession, PROOFSESSION_SIZE);
      exitLa(PROOFSESSION_SIZE);
      #endif

      break;

      /*****************
       * START RESPONSES 
       *****************/

#ifdef SODER

    case INS_START_RESPONSES:
      if (!CheckCase(3))
        exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_WORKING)
        exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < 6)
        exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ 6);
      
      checkPin(apdu_data.start_responses_in.pin);

      temp_prover_id = apdu_data.start_responses_in.prover_id;

      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
        exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

      if (!provers[temp_prover_id-1].exists)
        exitSW(ERR_PROVER_DOES_NOT_EXIST);

      if (current_prover_id != temp_prover_id)
        exitSW(ERR_PROOF_SESSION_CANNOT_START);

      if (provers[temp_prover_id-1].proofstatus != 1)
        exitSW(ERR_PROOF_SESSION_CANNOT_START);

      d = apdu_data.start_responses_in.input[0];
      
      if(Lc-6 < (d * 16))
        exitSW(ERR_MALICIOUS_INPUT_RESPONSE_STAGE);

      exit = 1;
      for (i=0; i<d; i++) {
        if (memcmp(provers[temp_prover_id-1].proofsession, &(apdu_data.start_responses_in.input[1]) + (16*i), PROOFSESSION_SIZE) == 0) {
          exit = 0;
          break;
        }
      }
      if (exit)
        exitSW(ERR_MALICIOUS_INPUT_RESPONSE_STAGE);

      if (P1 == 0x00) {

        // for idemix

        SHA256(temp_buffer, Lc-PIN_SIZE-1, apdu_data.start_responses_in.input);

      } else {

        // for uprove

        temp_size = 0;
        memcpy(temp_buffer, &temp_size, 2);                                                   // tem_buffer = 0x00 || 0x00
        temp_size = 1+16*d;
        memcpy(temp_buffer+2, &temp_size, 2);                                                 // tem_buffer = 0x00 || 0x00 || 1+16d over 2 bytes
        memcpy(temp_buffer+4, apdu_data.start_responses_in.input, 1+16*d);                    // tem_buffer = 0x00 || 0x00 || 1+16d over 2 bytes || d || ps_1 || ... || ps_d
        temp_size = 0;
        memcpy(temp_buffer+4+1+16*d, &temp_size, 2);                                          // tem_buffer = 0x00 || 0x00 || 1+16d over 2 bytes || d || ps_1 || ... || ps_d || 0x00 || 0x00
        temp_size = (Lc-5)-(1+16*d); // temp_size = size(h)
        memcpy(temp_buffer+6+1+16*d, &temp_size, 2);                                          // tem_buffer = 0x00 || 0x00 || 1+16d over 2 bytes || d || ps_1 || ... || ps_d || 0x00 || 0x00 || size(h) over 2 bytes
        memcpy(temp_buffer+8+1+16*d, apdu_data.start_responses_in.input+(1+16*d), temp_size); // tem_buffer = 0x00 || 0x00 || 1+16d over 2 bytes || d || ps_1 || ... || ps_d || 0x00 || 0x00 || size(h) over 2 bytes || h

        SHA256(temp_buffer, 8+1+16*d+temp_size, temp_buffer);

      }
      memset(provers[temp_prover_id-1].c, 0, HASH_SIZE);
      memcpy(provers[temp_prover_id-1].c+HASH_SIZE-provers[temp_prover_id-1].csize, temp_buffer, provers[temp_prover_id-1].csize);

      provers[temp_prover_id-1].proofstatus = 2;

      break;

#else

    case INS_START_RESPONSES:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < 6)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA ^ 6);
      
      checkPin(apdu_data.start_responses_in.pin);
      
      temp_prover_id = apdu_data.start_responses_in.prover_id;
      
      if (temp_prover_id < 1 || temp_prover_id > NUM_PROVERS)
	exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);
      
      if (!provers[temp_prover_id-1].exists)
	exitSW(ERR_PROVER_DOES_NOT_EXIST);
      
      if (current_prover_id != temp_prover_id)
	exitSW(ERR_PROOF_SESSION_CANNOT_START);
      
      if (provers[temp_prover_id-1].proofstatus != 1)
	exitSW(ERR_PROOF_SESSION_CANNOT_START);
      
      if((Lc-PIN_SIZE-1) != provers[temp_prover_id-1].csize) 
	exitSW(ERR_MALICIOUS_INPUT_RESPONSE_STAGE);
      
      memcpy(temp_buffer, apdu_data.start_responses_in.input, provers[temp_prover_id-1].csize);
      
      memset(provers[temp_prover_id-1].c, 0, HASH_SIZE);
      memcpy(provers[temp_prover_id-1].c+HASH_SIZE-provers[temp_prover_id-1].csize, temp_buffer, provers[temp_prover_id-1].csize);
      
      provers[temp_prover_id-1].proofstatus = 2;
      
      break;

#endif
      
      /*****************
       * SET CREDENTIAL
       *****************/

    case INS_SET_CREDENTIAL:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE + 2)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + 2));
      
      checkPin(apdu_data.set_credential_in.pin);

      temp_credential_id = apdu_data.set_credential_in.credential_id;

      if (temp_credential_id < 1 || temp_credential_id > NUM_CREDS)
	exitSW(ERR_CREDENTIALID_OUTSIDE_OF_RANGE);
      
      temp_issuer_id = apdu_data.set_credential_in.issuer_id;
      
      if (temp_issuer_id < 1 || temp_issuer_id > NUM_ISSUERS)
	exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
      
      if (!issuers[temp_issuer_id-1].exists)
	exitSW(ERR_ISSUER_DOES_NOT_EXIST);

      credentials[temp_credential_id-1].credential_id = temp_credential_id;
      credentials[temp_credential_id-1].issuer_id = temp_issuer_id;
      memset(credentials[temp_credential_id-1].v, 0, MAX_SMALLINT_SIZE-x_size);
#ifdef TEST_MODE
      memset(credentials[temp_credential_id-1].v+MAX_SMALLINT_SIZE-x_size, 0xaa, x_size);
#else
      getRandomBytes(credentials[temp_credential_id-1].v+MAX_SMALLINT_SIZE-x_size, x_size);
#endif
      credentials[temp_credential_id-1].v_size = x_size;

      memset(credentials[temp_credential_id-1].kv, 0, MAX_SMALLINT_SIZE);
      credentials[temp_credential_id-1].kv_size = 0;
      credentials[temp_credential_id-1].status = 0;
      credentials[temp_credential_id-1].prescount = 0;
      credentials[temp_credential_id-1].exists = 1;

      break;

      /******************
       * LIST CREDENTIALS
       ******************/

    case INS_LIST_CREDENTIALS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);
      
      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ PIN_SIZE);
      
      checkPin(apdu_data.pin);

      temp_size = 0;

      for (temp_credential_id = 1; temp_credential_id < NUM_CREDS; temp_credential_id++) 
	if (credentials[temp_credential_id-1].exists) {
	  temp_buffer[temp_size] = temp_credential_id;
	  temp_size++;
	}
      
      if (temp_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_size);
	exitLa(temp_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_size;
	output_large_data();
      }

      break;

      /*****************
       * READ CREDENTIAL
       *****************/

    case INS_READ_CREDENTIAL:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);

      apdu_data.dataout[0] = credentials[temp_credential_id-1].issuer_id;
      memcpy(apdu_data.dataout+1, &(credentials[temp_credential_id-1].v_size), 2);
      memcpy(apdu_data.dataout+3, &(credentials[temp_credential_id-1].kv_size), 2);
      memcpy(apdu_data.dataout+5, &(credentials[temp_credential_id-1].status), 2); // status || prescount

      exitLa(7);
      
      break;

      /*******************
       * REMOVE CREDENTIAL
       *******************/

    case INS_REMOVE_CREDENTIAL:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);

      memset(&(credentials[temp_credential_id-1].credential_id), 0, sizeof(CREDENTIAL));
      
      break;

      /***************************
       * GET CREDENTIAL PUBLIC KEY
       ***************************/

    case INS_GET_CREDENTIAL_PUBLIC_KEY:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);
      
      temp_issuer_id = credentials[temp_credential_id-1].issuer_id;
      
      singleOrDoubleExpo(temp_issuer_id, device_key, x_size, credentials[temp_credential_id-1].v, credentials[temp_credential_id-1].v_size);

      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer+MAX_BIGINT_SIZE-buffer_size, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer+MAX_BIGINT_SIZE-buffer_size;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;

      /*************************
       * GET ISSUANCE COMMITMENT
       *************************/

    case INS_GET_ISSUANCE_COMMITMENT:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);
      
      temp_issuer_id = credentials[temp_credential_id-1].issuer_id;
      temp_status = credentials[temp_credential_id-1].status;

      if (temp_status != 0)
	exitSW(ERR_CREDENTIAL_INAP_STATE);

      accessSession(temp_credential_id);

      if (provers[current_prover_id-1].proofstatus != 1)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      memset(temp_key, 0, MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize);
#ifdef TEST_MODE
      memset(temp_key + MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize, 0xbb, provers[current_prover_id-1].ksize);
#else
      getRandomBytes(temp_key + MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize, provers[current_prover_id-1].ksize);
#endif
      temp_key_size = provers[current_prover_id-1].ksize;
      
      singleOrDoubleExpo(temp_issuer_id, provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize, temp_key+MAX_BIGINT_SIZE-MAX_SMALLINT_SIZE, provers[current_prover_id-1].ksize);

      credentials[temp_credential_id-1].status = 1;
      memcpy(credentials[temp_credential_id-1].kv, temp_key+MAX_BIGINT_SIZE-MAX_SMALLINT_SIZE, MAX_SMALLINT_SIZE);
      credentials[temp_credential_id-1].kv_size = temp_key_size;

      memset(&temp_key, 0, MAX_BIGINT_SIZE);


      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer+MAX_BIGINT_SIZE-buffer_size, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer+MAX_BIGINT_SIZE-buffer_size;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;


      /***********************
       * GET ISSUANCE RESPONSE
       ***********************/

    case INS_GET_ISSUANCE_RESPONSE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);
      
      temp_issuer_id = credentials[temp_credential_id-1].issuer_id;
      temp_status = credentials[temp_credential_id-1].status;

      if (temp_status != 1)
	exitSW(ERR_CREDENTIAL_INAP_STATE);

      accessSession(temp_credential_id);

      if (provers[current_prover_id-1].proofstatus != 2)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      singleOrDoubleResponse(temp_issuer_id, 
			     provers[current_prover_id-1].c, provers[current_prover_id-1].csize, 
			     device_key, x_size, 
			     provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize, 
			     credentials[temp_credential_id-1].v, credentials[temp_credential_id-1].v_size, 
			     credentials[temp_credential_id-1].kv, provers[current_prover_id-1].ksize);

      credentials[temp_credential_id-1].status = 2;

      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;

      /*****************************
       * GET PRESENTATION COMMITMENT
       *****************************/

    case INS_GET_PRESENTATION_COMMITMENT:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);
      
      temp_issuer_id = credentials[temp_credential_id-1].issuer_id;
      temp_status = credentials[temp_credential_id-1].status;

      if (temp_status != 2)
	exitSW(ERR_CREDENTIAL_INAP_STATE);

      accessSession(temp_credential_id);

      if (provers[current_prover_id-1].proofstatus != 1)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      if (temp_issuer_id < 1 || temp_issuer_id > NUM_ISSUERS)
	exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
      
      if (!issuers[temp_issuer_id-1].exists)
	exitSW(ERR_ISSUER_DOES_NOT_EXIST);

      #if NUM_COUNTERS > 0
      temp_counter_id = issuers[temp_issuer_id-1].counter_id;

      if (temp_counter_id != 0) {
	
	if (temp_counter_id < 1 || temp_counter_id > NUM_COUNTERS)
	  exitSW(ERR_COUNTER_ID_OUTSIDE_OF_RANGE);

	if (!counters[temp_counter_id-1].exists)
	  exitSW(ERR_COUNTER_DOES_NOT_EXIST);

	if (counters[temp_counter_id-1].index < counters[temp_counter_id-1].threshold)
	  exitSW(ERR_PRESENTATION_CRED_RESTRICTED_BY_IMM_COUNTER);

      }
      #endif

      memset(temp_key, 0, MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize);
#ifdef TEST_MODE
      memset(temp_key + MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize, 0xaa, provers[current_prover_id-1].ksize);
#else
      getRandomBytes(temp_key + MAX_BIGINT_SIZE-provers[current_prover_id-1].ksize, provers[current_prover_id-1].ksize);
#endif

      singleOrDoubleExpo(temp_issuer_id, provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize, temp_key+MAX_BIGINT_SIZE-MAX_SMALLINT_SIZE, provers[current_prover_id-1].ksize);

      memcpy(credentials[temp_credential_id-1].kv, temp_key+MAX_BIGINT_SIZE-MAX_SMALLINT_SIZE, MAX_SMALLINT_SIZE);
      credentials[temp_credential_id-1].status = 3;

      memset(&temp_key, 0, MAX_BIGINT_SIZE);
      
      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer+MAX_BIGINT_SIZE-buffer_size, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer+MAX_BIGINT_SIZE-buffer_size;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;

      /***************************
       * GET PRESENTATION RESPONSE
       ***************************/

    case INS_GET_PRESENTATION_RESPONSE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      temp_credential_id = accessCredential(apdu_data.pin_and_credential_id.pin, apdu_data.pin_and_credential_id.credential_id);
      
      temp_issuer_id = credentials[temp_credential_id-1].issuer_id;
      temp_status = credentials[temp_credential_id-1].status;

      if (temp_status != 3)
	exitSW(ERR_CREDENTIAL_INAP_STATE);

      accessSession(temp_credential_id);

      if (provers[current_prover_id-1].proofstatus != 2)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      singleOrDoubleResponse(temp_issuer_id, 
			     provers[current_prover_id-1].c, provers[current_prover_id-1].csize, 
			     device_key, x_size, 
			     provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize,
			     credentials[temp_credential_id-1].v, credentials[temp_credential_id-1].v_size, 
			     credentials[temp_credential_id-1].kv, provers[current_prover_id-1].ksize);

      credentials[temp_credential_id-1].prescount += 1;

      if (issuers[temp_issuer_id-1].numpres == 0 || credentials[temp_credential_id-1].prescount < issuers[temp_issuer_id-1].numpres)
	credentials[temp_credential_id-1].status = 2;
      else
	credentials[temp_credential_id-1].status = 4;

      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;

      /***********************
       * GET DEVICE PUBLIC KEY
       ***********************/

    case INS_GET_DEVICE_PUBLIC_KEY:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      getGenerator(0, 1);
      temp_gen_1_size = buffer_size;

      // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
      if (temp_gen_1_size >= groups[0].modulus_size)
	multosModularReduction (temp_gen_1_size, groups[0].modulus_size, buffer+MAX_BIGINT_SIZE-temp_gen_1_size, groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size);
      
      // We have that temp_gen_1_size <= modulus_size
      // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
      crxModularExponentiation (x_size,
				groups[0].modulus_size, 
				device_key+MAX_SMALLINT_SIZE-x_size, 
				groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size, 
				buffer+MAX_BIGINT_SIZE-groups[0].modulus_size, // ON POURRAIT METTRE buffer+MAX_BIGINT_SIZE-temp_gen_1_size ????? Bog, la fonction considere que tout est de taille du modulus
				temp_buffer);
      temp_buffer_size = groups[0].modulus_size;
      
      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /***********************
       * GET DEVICE COMMITMENT
       ***********************/

    case INS_GET_DEVICE_COMMITMENT:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < PIN_SIZE)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      accessSession(0);

      // fetch provers[current_prover_id-1].kx, provers[current_prover_id-1].proofstatus

      if (provers[current_prover_id-1].proofstatus != 1)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      // fetch groups[0].modulus

      getGenerator(0, 1);

      // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
      if (buffer_size >= groups[0].modulus_size) {
	multosModularReduction (buffer_size, groups[0].modulus_size, buffer+MAX_BIGINT_SIZE-buffer_size, groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size);
	buffer_size = groups[0].modulus_size;
      }

      // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
      crxModularExponentiation (provers[current_prover_id-1].ksize, 
				groups[0].modulus_size, 
				provers[current_prover_id-1].kx+MAX_SMALLINT_SIZE-provers[current_prover_id-1].ksize, 
				groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size, 
				buffer+MAX_BIGINT_SIZE-groups[0].modulus_size, 
				temp_buffer);
      temp_buffer_size = groups[0].modulus_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /*********************
       * GET DEVICE RESPONSE
       *********************/

    case INS_GET_DEVICE_RESPONSE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc != PIN_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ PIN_SIZE);

      checkPin(apdu_data.pin);

      accessSession(0);

      // fetch provers[current_prover_id-1].kx, provers[current_prover_id-1].c, provers[current_prover_id-1].proofstatus

      if (provers[current_prover_id-1].proofstatus != 2)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      // fetch groups[0].q

      singleResponse(provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize, 
		     provers[current_prover_id-1].c, provers[current_prover_id-1].csize, 
		     device_key, x_size, 
		     groups[0].q, groups[0].q_size, 0);

      if (buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, buffer, buffer_size);
	exitLa(buffer_size);
      } else {
	remaining_position = buffer;
	remaining_size = buffer_size;
	output_large_data();
      }

      break;

      /*******************************
       * GET SCOPE-EXCLUSIVE PSEUDONYM
       *******************************/

    case INS_GET_SCOPE_EXCLUSIVE_PSEUDONYM:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < PIN_SIZE + 1)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + 1));

      checkPin(apdu_data.get_scope_exclusive_pseudonym_in.pin);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      // fetch groups[0].modulus, groups[0].f

      scopeExclusiveGenerator(apdu_data.get_scope_exclusive_pseudonym_in.scope, Lc-PIN_SIZE, groups[0].modulus, groups[0].modulus_size, groups[0].f, groups[0].f_size);

      if (buffer_size >= groups[0].modulus_size) {
	// void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
	multosModularReduction (buffer_size, groups[0].modulus_size, buffer+MAX_BIGINT_SIZE-buffer_size, groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size);
	buffer_size = groups[0].modulus_size;
      }

      // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
      crxModularExponentiation(x_size, groups[0].modulus_size, device_key+MAX_SMALLINT_SIZE-x_size, groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size, buffer+MAX_BIGINT_SIZE-groups[0].modulus_size, temp_buffer);
      temp_buffer_size = groups[0].modulus_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /********************************
       * GET SCOPE-EXCLUSIVE COMMITMENT
       ********************************/

    case INS_GET_SCOPE_EXCLUSIVE_COMMITMENT:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < PIN_SIZE + 1)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + 1));

      checkPin(apdu_data.get_scope_exclusive_commitment_in.pin);

      accessSession(0);

      // fetch provers[current_prover_id-1].kx, provers[current_prover_id-1].proofstatus

      if (provers[current_prover_id-1].proofstatus != 1)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      // fetch groups[0].modulus, groups[0].f

      scopeExclusiveGenerator(apdu_data.get_scope_exclusive_commitment_in.scope, Lc - PIN_SIZE, groups[0].modulus, groups[0].modulus_size, groups[0].f, groups[0].f_size);

      // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
      if (buffer_size >= groups[0].modulus_size) {
	multosModularReduction (buffer_size, groups[0].modulus_size, buffer+MAX_BIGINT_SIZE-buffer_size, groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size);
	buffer_size = groups[0].modulus_size;
      }

      // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
      crxModularExponentiation (provers[current_prover_id-1].ksize, 
				groups[0].modulus_size, 
				provers[current_prover_id-1].kx+MAX_SMALLINT_SIZE-provers[current_prover_id-1].ksize,
				groups[0].modulus+MAX_BIGINT_SIZE-groups[0].modulus_size, 
				buffer+MAX_BIGINT_SIZE-groups[0].modulus_size, 
				temp_buffer);
      temp_buffer_size = groups[0].modulus_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;


      /******************************
       * GET SCOPE-EXCLUSIVE RESPONSE
       ******************************/

    case INS_GET_SCOPE_EXCLUSIVE_RESPONSE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);
      
      if (Lc < PIN_SIZE + 1)
	exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA  ^ (PIN_SIZE + 1));

      checkPin(apdu_data.get_scope_exclusive_response_in.pin);

      accessSession(0);

      // fetch provers[current_prover_id-1].kx, provers[current_prover_id-1].c, provers[current_prover_id-1].proofstatus

      if (provers[current_prover_id-1].proofstatus != 2)
	exitSW(ERR_CURRENT_PROOF_SESSION_INAP_STAGE);

      if (!groupExists(0))
	exitSW(ERR_GROUP_DOES_NOT_EXIST);

      // fetch groups[0].q

      singleResponse(provers[current_prover_id-1].kx, provers[current_prover_id-1].ksize, provers[current_prover_id-1].c , provers[current_prover_id-1].csize, device_key, x_size, groups[0].q, groups[0].q_size, 0);

      memcpy(temp_buffer, buffer, buffer_size);
      temp_buffer_size = buffer_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /************
       * STORE BLOB
       ************/
      
    case INS_STORE_BLOB:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);

      uri = accessURI(apdu_data.blob_in.datain, Lc);      

      getBlobstoreInformations(&temp_blob_index, &temp_blobcount, &temp_uri_index, uri, Lc-PIN_SIZE);

      if (temp_blobcount == MAX_NUMBER_OF_BLOBS)
	exitSW(ERR_MAX_NBR_BLOB_REACHED);

      if (temp_uri_index != MAX_NUMBER_OF_BLOBS) {
	temp_blob_index = temp_uri_index; // there already exists a blob with this uri, overwrite it
      }

      if (buffer_size > MAX_BLOB_SIZE)
	exitSW(ERR_BLOB_TOO_LARGE);

      blob_catalog[temp_blob_index].exists = 1;
      memcpy(blob_catalog[temp_blob_index].uri, uri, Lc-PIN_SIZE);
      blob_catalog[temp_blob_index].uri_size = Lc-PIN_SIZE;
      blob_catalog[temp_blob_index].buffer_size = buffer_size;

      segmentToStaticHigh(blob_store[temp_blob_index].buffer, buffer, buffer_size);

      break;

      /************
       * LIST BLOBS
       ************/
      
    case INS_LIST_BLOBS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + 1)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + 1));

      checkPin(apdu_data.list_blobs_in.pin);

      getBlobstoreInformations(&temp_blob_index, &temp_blobcount, &temp_uri_index, NULL, NULL);

      buffer_size = 0;

      temp_nread = apdu_data.list_blobs_in.nread;
      prev_nread = temp_nread;

      // if prev_nread > temp_blobcount, the reader is asking for more
      // blobs although they do not exist. Exit with  "blobcount on 1 byte" 00 90 00.
      if (prev_nread >= temp_blobcount) {
	apdu_data.dataout[0] = temp_blobcount & 0xFF;
	apdu_data.dataout[1] = 0x00;
	exitLa(2);
      }

      temp_size = 0;

      i = 0;
      while(i < MAX_NUMBER_OF_BLOBS && (temp_nread < temp_blobcount) && !(temp_size + blob_catalog[temp_nread].uri_size + 1 > MAX_APDU_OUTPUT_DATA_SIZE - 2)) {

	if(blob_catalog[i].exists) {

	  if(prev_nread) {

	    prev_nread--;

	  } else {

	    memcpy(apdu_data.dataout+temp_size, &(blob_catalog[i].uri_size), 1);
	    temp_size += 1;
	    memcpy(apdu_data.dataout+temp_size, blob_catalog[i].uri, blob_catalog[i].uri_size);
	    temp_size += blob_catalog[i].uri_size;
	    
	    temp_nread++;

	  }

	}

	i++;
      }

      memcpy(apdu_data.dataout+temp_size, &temp_nread, 1);
      temp_size += 1;
      temp_nread = temp_blobcount - temp_nread;
      memcpy(apdu_data.dataout+temp_size, &temp_nread, 1);
      temp_size += 1;

      exitLa(temp_size); // in this particular case, we know that no more than 255 will ever be sent out

      break;

      /***********
       * READ BLOB
       ***********/
      
    case INS_READ_BLOB:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      uri = accessURI(apdu_data.blob_in.datain, Lc);

      getBlobstoreInformations(&temp_blob_index, &temp_blobcount, &temp_uri_index, uri, Lc-PIN_SIZE);

      if (temp_uri_index == MAX_NUMBER_OF_BLOBS)
	exitSW(ERR_NO_BLOB_WITH_GIVEN_URI);
      
      temp_buffer_size = blob_catalog[temp_uri_index].buffer_size;
      staticHighToSegment(temp_buffer, blob_store[temp_uri_index].buffer, temp_buffer_size);

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /*************
       * REMOVE BLOB
       *************/
      
    case INS_REMOVE_BLOB:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);

      uri = accessURI(apdu_data.blob_in.datain, Lc);

      getBlobstoreInformations(&temp_blob_index, &temp_blobcount, &temp_uri_index, uri, Lc-PIN_SIZE);

      if (temp_uri_index == MAX_NUMBER_OF_BLOBS)
	exitSW(ERR_NO_BLOB_WITH_GIVEN_URI);

      blob_catalog[temp_uri_index].exists = 0;
      memset(blob_catalog[temp_uri_index].uri, 0, MAX_URI_SIZE);
      blob_catalog[temp_uri_index].uri_size = 0;
      blob_catalog[temp_uri_index].buffer_size = 0;

      break;

      /***************
       * BACKUP DEVICE
       ***************/
      
    case INS_BACKUP_DEVICE:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE));
      
      checkPin(apdu_data.backup_in.pin);

      buffer_size = 0;
      memcpy(buffer+buffer_size, pin, PIN_SIZE);
      buffer_size += PIN_SIZE;
      memcpy(buffer+buffer_size, puk, PUK_SIZE);
      buffer_size += PUK_SIZE;
      #ifdef SODER
      memcpy(buffer+buffer_size, device_key+MAX_SMALLINT_SIZE-x_size, x_size);
      buffer_size += x_size;
      #endif

      encrypt(apdu_data.backup_in.password, 0x01);

      memcpy(temp_buffer, buffer, buffer_size);
      temp_buffer_size = buffer_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;

      /****************
       * RESTORE DEVICE
       ****************/
      
    case INS_RESTORE_DEVICE:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE));
      
      checkPin(apdu_data.backup_in.pin);

      decrypt(device_id_prim, apdu_data.backup_in.password, 0x01);

      memcpy(pin, buffer, PIN_SIZE);
      memcpy(puk, buffer+PIN_SIZE, PUK_SIZE);

      #ifdef SODER
      x_size = buffer_size - PIN_SIZE - PUK_SIZE;
      memset(device_key, 0, MAX_SMALLINT_SIZE);
      memcpy(device_key+MAX_SMALLINT_SIZE-x_size, buffer+PIN_SIZE+PUK_SIZE, x_size);
      #endif

      break;

      /*****************
       * BACKUP COUNTERS
       *****************/
      
    #if NUM_COUNTERS > 0
    case INS_BACKUP_COUNTERS:
      if (!CheckCase(4))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE));
      
      checkPin(apdu_data.backup_in.pin);

      buffer_size = 0;

      for (temp_counter_id = 1; temp_counter_id <= NUM_COUNTERS; temp_counter_id++) {
	if (counters[temp_counter_id-1].exists) {
	  memcpy(buffer+buffer_size, &(counters[temp_counter_id-1].counter_id), 1);
	  buffer_size++;
	  memcpy(buffer+buffer_size, &(counters[temp_counter_id-1].index), 1);
	  buffer_size++;
	  memcpy(buffer+buffer_size, counters[temp_counter_id-1].cursor, CURSOR_SIZE);
	  buffer_size += CURSOR_SIZE;
	}
      }

      if (buffer_size == 0)
	exitSW(ERR_NO_CONTENT_TO_BACKUP);

      encrypt(apdu_data.backup_in.password, 0x02);
      
      memcpy(temp_buffer, buffer, buffer_size);
      temp_buffer_size = buffer_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
	memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
	exitLa(temp_buffer_size);
      } else {
	remaining_position = temp_buffer;
	remaining_size = temp_buffer_size;
	output_large_data();
      }

      break;
    #endif

      /******************
       * RESTORE COUNTERS
       ******************/

    #if NUM_COUNTERS > 0
    case INS_RESTORE_COUNTERS:
      if (!CheckCase(3))
	exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
	exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE)
	exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE));
      
      checkPin(apdu_data.backup_in.pin);

      decrypt(device_id_prim, apdu_data.backup_in.password, 0x02);

      if (buffer_size % 6 != 0)
	exitSW(ERR_INVALID_BACKUP_ARCHIVE);

      d = buffer_size / 6;

      for (i = 0; i < d; i++) {
	memcpy(mem_session.small_buffer, buffer + 6*i, 6);
	// small_buffer contains counter_id (1 byte) || index (1 byte) || cursor (4 bytes)
	temp_counter_id = mem_session.small_buffer[0];
	if (!counters[temp_counter_id-1].exists)
	  exitSW(ERR_COUNTER_DOES_NOT_EXIST);
	counters[temp_counter_id-1].index = mem_session.small_buffer[1];
	memcpy(counters[temp_counter_id-1].cursor, mem_session.small_buffer+2, CURSOR_SIZE);
      }

      break;
    #endif

      /********************
       * BACKUP CREDENTIALS
       ********************/

    #ifdef SODER
    case INS_BACKUP_CREDENTIAL:
      if (!CheckCase(4))
        exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
        exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE + 1)
        exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE + 1));
      
      checkPin(apdu_data.backup_credential_in.pin);

      temp_credential_id = apdu_data.backup_credential_in.credential_id;

      if (temp_credential_id < 1 || temp_credential_id > NUM_CREDS)
        exitSW(ERR_CREDENTIALID_OUTSIDE_OF_RANGE);

      if (!credentials[temp_credential_id - 1].exists)
        exitSW(ERR_CREDENTIAL_DOES_NOT_EXIST);

      // fetch credentials[temp_credential_id - 1].issuer_id, credentials[temp_credential_id - 1].status, credentials[temp_credential_id - 1].prescount, credentials[temp_credential_id - 1].v
      
      if (credentials[temp_credential_id - 1].status != 2) 
        exitSW(ERR_NO_CONTENT_TO_BACKUP);

      buffer_size = 0;
      memcpy(buffer+buffer_size, &(credentials[temp_credential_id - 1].credential_id), 1);
      buffer_size += 1;
      memcpy(buffer+buffer_size, &(credentials[temp_credential_id - 1].issuer_id), 1);
      buffer_size += 1;
      memcpy(buffer+buffer_size, &(credentials[temp_credential_id - 1].status), 1);
      buffer_size += 1;
      memcpy(buffer+buffer_size, &(credentials[temp_credential_id - 1].prescount), 1);
      buffer_size += 1;
      memcpy(buffer+buffer_size, credentials[temp_credential_id - 1].v, credentials[temp_credential_id - 1].v_size);
      buffer_size += credentials[temp_credential_id - 1].v_size;

      encrypt(apdu_data.backup_credential_in.password, 0x03);

      memcpy(temp_buffer, buffer, buffer_size);
      temp_buffer_size = buffer_size;

      if (temp_buffer_size <= MAX_APDU_OUTPUT_DATA_SIZE) {
        memcpy(apdu_data.dataout, temp_buffer, temp_buffer_size);
        exitLa(temp_buffer_size);
      } else {
        remaining_position = temp_buffer;
        remaining_size = temp_buffer_size;
        output_large_data();
      }

      break;
      #endif

      /*********************
       * RESTORE CREDENTIALS
       *********************/
      
    #ifdef SODER
    case INS_RESTORE_CREDENTIAL:
      if (!CheckCase(3))
        exitSW(ERR_BAD_ISO);

      if (mode != MODE_ROOT && mode != MODE_WORKING)
        exitSW(ERR_BAD_MODE ^ mode);

      if (Lc != PIN_SIZE + PASSWORD_SIZE)
        exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA ^ (PIN_SIZE + PASSWORD_SIZE));
      
      checkPin(apdu_data.backup_in.pin);

      decrypt(device_id_prim, apdu_data.backup_in.password, 0x03);

      // buffer should contain credential_id || issuer_id || status || prescount || v

      if (buffer[2] != 2)
        exitSW(ERR_INVALID_BACKUP_ARCHIVE);

      temp_credential_id = buffer[0];

      memcpy(&(credentials[temp_credential_id - 1].credential_id), buffer, 2); /* This sets credential_id AND issuer_id */
      /*       credentials[temp_credential_id - 1].credential_id = temp_credential_id; */
      /*       credentials[temp_credential_id - 1].issuer_id = buffer[1]; */
      memcpy(&(credentials[temp_credential_id - 1].status), buffer+2, 2); /* This sets status AND prescount */
      /*       credentials[temp_credential_id - 1].status    = buffer[2]; */
      /*       credentials[temp_credential_id - 1].prescount = buffer[3]; */
      memcpy(credentials[temp_credential_id - 1].v, buffer+4, buffer_size-4);
      credentials[temp_credential_id - 1].v_size = buffer_size-4;
      memset(credentials[temp_credential_id - 1].kv, 0, MAX_SMALLINT_SIZE);
      credentials[temp_credential_id - 1].kv_size = 0;
      credentials[temp_credential_id - 1].exists = 1;

      break;
      #endif

      /**********
       * GET INFO
       **********/

    case INS_GET_INFO:
      if (!CheckCase(2)) 
	exitSW(ERR_BAD_ISO);

      temp_buffer_size = 0;

      temp_size = CHALLENGE_MAX_SIZE;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = NUM_ISSUERS;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = MAX_NUMBER_OF_BLOBS;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = NUM_GROUPS;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = NUM_COUNTERS;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = NUM_AUTH_KEYS;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = MAX_URI_SIZE;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = MAX_BLOB_SIZE;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      temp_size = MAX_BIGINT_SIZE;
      memcpy(apdu_data.dataout+temp_buffer_size, &temp_size, 2);
      temp_buffer_size += 2;

      exitLa(temp_buffer_size);

      break;
      

      /**************
       * GET RESPONSE
       **************/

    case INS_GET_RESPONSE:
      if (!CheckCase(2)) 
	exitSW(ERR_BAD_ISO);

      output_large_data();

      break;

      /*********
       * NOTHING
       *********/

    case INS_NOTHING:

      break;

      /*********
       * DEFAULT
       *********/

    default:
      exitSW(ERR_BAD_INS);

    }



}      


/*******************************************************************************************************************************************************/
/*******************************************************************************************************************************************************/
/*************************************************             Subroutines                **************************************************************/
/*******************************************************************************************************************************************************/
/*******************************************************************************************************************************************************/


/************************************************************************************************************************************************
 * void getRandomBytes(BYTE* buffer, unsigned int size)
 *
 * Generate 's = 8*ceil(size/8)' random bytes and put them in
 * buffer[0],...,buffer[s-1].
 ************************************************************************************************************************************************/

void getRandomBytes(BYTE* buffer, unsigned int size) {

  unsigned int i;
  BYTE temp_buffer[8];
  
  for (i=8; i<=size; i += 8) {
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(8, temp_buffer, buffer + i - 8);
  }

  switch (8 - i + size) {
  case 1:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(1, temp_buffer, buffer + i - 8);
    break;
  case 2:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(2, temp_buffer, buffer + i - 8);
    break;
  case 3:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(3, temp_buffer, buffer + i - 8);
    break;
  case 4:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(4, temp_buffer, buffer + i - 8);
    break;
  case 5:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(5, temp_buffer, buffer + i - 8);
    break;
  case 6:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(6, temp_buffer, buffer + i - 8);
    break;
  case 7:
    multosGetRandomNumber(temp_buffer);
    multosBlockCopyFixedLength(7, temp_buffer, buffer + i - 8);
    break;
  default:
    break;
  }
  
}

/************************************************************************************************************************************************
 * void checkPin(BYTE* tested_pin)
 *
 * If tested_pin == pin, this routine returns to the
 * caller. Otherwise, it sends a SW != 0x9000. This routine does NOT
 * check whether pin_trials > 0.
 ************************************************************************************************************************************************/

void checkPin(BYTE* tested_pin) {

  if (memcmp(tested_pin, pin, PIN_SIZE) == 0) {
    pin_trials = MAX_PIN_TRIALS;
    return;
  }

  // if this point is reached, the pin is incorrect. We eventually
  // exit.

  pin_trials -= 1;
  
  if (pin_trials == 0) {
    mode = MODE_LOCKED;
    exitSW(ERR_INCORRECT_PIN_AND_CARD_LOCKED);
  }

  exitSW(ERR_INCORRECT_PIN);
  
}

/************************************************************************************************************************************************
 * void checkPuk(BYTE* tested_puk)
 *
 * If tested_puk == pin, this routine returns to the
 * caller. Otherwise, it sends a SW != 0x9000. This routine does NOT
 * check whether puk_trials > 0.
 ************************************************************************************************************************************************/

void checkPuk(BYTE* tested_puk) {

  if (memcmp(tested_puk, puk, PUK_SIZE) == 0) {
    puk_trials = MAX_PUK_TRIALS;
    return;
  }

  // if this point is reached, the puk is incorrect. We eventually
  // exit.

  puk_trials -= 1;
  
  if (puk_trials == 0) {
    mode = MODE_DEAD;
    exitSW(ERR_INCORRECT_PUK_AND_CARD_DEAD);
  }

  exitSW(ERR_INCORRECT_PUK);
  
}

/************************************************************************************************************************************************
 * unsigned int sizeDecode(BYTE *s)
 *
 * Take a n-byte table of BYTEs and returns
 * 2^8 * s[0] + s[1]. This assumes that SIZE_SIZE == 2.
 ************************************************************************************************************************************************/

unsigned int sizeDecode(BYTE *s) {

  return (unsigned int)( (((WORD)(s[0]))<<8) + s[1]);

}

/************************************************************************************************************************************************
 * void sizeEncode(BYTE *s, unsigned int size) {
 *
 * This is the inverse of sizeDecode
 ************************************************************************************************************************************************/

void sizeEncode(BYTE *s, unsigned int size) {

  s[1] = size & 0xFF;
  s[0] = (size >> 8) & 0xFF;

}

/************************************************************************************************************************************************
 * void getKey(BYTE *key, key_id)
 *
 ************************************************************************************************************************************************/

void getKey(BYTE *key, unsigned int *key_size, const BYTE key_id) {

  if (key_id > NUM_ISSUERS)
    exitSW(ERR_KEY_ID_OUTSIDE_RANGE);

  if (!auth_keys_sizes[key_id])
    exitSW(ERR_AUTHENTICATION_KEY_DOES_NOT_EXIST);

  *key_size = auth_keys_sizes[key_id];
  memcpy(key, auth_keys[key_id], auth_keys_sizes[key_id]);

}

/************************************************************************************************************************************************
 * void publicKeyEncrypt(BYTE* key, unsigned int key_size)
 *
 * Overwrites : temp_buffer, temp_buffer_size ???
 ************************************************************************************************************************************************/

void publicKeyEncrypt(BYTE* key, unsigned int key_size) {
  
  if (temp_buffer_size > key_size - 43)
    exitSW(ERR_AUTHENTICATION_KEY_TOO_SHORT);

  encryption(temp_buffer, &temp_buffer_size, temp_buffer, temp_buffer_size, key, key_size);

}

/************************************************************************************************************************************************
 * void encryption(BYTE* dst, unsigned int* dst_size, const BYTE *src, unsigned int src_size, BYTE *key, unsigned int key_size)
 *
 * Overwrites : pad, temp_rand_size, 
 ************************************************************************************************************************************************/

void encryption(BYTE* dst, unsigned int* dst_size, const BYTE *src, const unsigned int src_size, const BYTE *key, const unsigned int key_size) {

  BYTE exponent[1] = {3};

  temp_rand_size = key_size - 35 - src_size; // should be 83

#ifdef TEST_MODE
  memset(mem_session.pad+3+src_size, 0xbc, temp_rand_size);
#else
  getRandomBytes(mem_session.pad+3+src_size, temp_rand_size);
#endif

  mem_session.pad[0] = 0x00;
  sizeEncode(mem_session.pad+1, src_size);
  memcpy(mem_session.pad+3, src, src_size);
  pad_size = key_size-32; // should be 96

  SHA256(mem_session.pad+pad_size, pad_size, mem_session.pad); // compute (pad || h)

  crxModularExponentiation(1, key_size, exponent, (BYTE*)key, mem_session.pad, dst);

  *dst_size = key_size;

  temp_rand_size = 0;

}

/************************************************************************************************************************************************
 * void extract(const BYTE *key, const unsigned int key_size) 
 ************************************************************************************************************************************************/

void extract(const BYTE *key, const unsigned int key_size) {

  extraction(key, key_size, buffer, &buffer_size, mem_session.challenge, challenge_size);

  memset(mem_session.challenge, 0, CHALLENGE_MAX_SIZE);

  if (buffer_size == 0)
    exitSW(ERR_DATA_AUTHENTICATION_FAILURE);    

}

/************************************************************************************************************************************************
 * void extraction(const BYTE *n, const unsigned int n_size, BYTE *sig, unsigned int sig_size, const BYTE *challenge)
 * 
 * In case of success, the extraction is put back into sig.
 ************************************************************************************************************************************************/

unsigned int extraction(const BYTE *n, const unsigned int n_size, BYTE *sig, unsigned int *sig_size, const BYTE *challenge, const unsigned int challenge_size) {

  BYTE exponent[1] = {3};
  BYTE hash_[HASH_SIZE]; // only used in the 'else' below
  BYTE hash_prime[HASH_SIZE];
  unsigned int pad_size;
  BYTE b;
  unsigned int L;
  BYTE *blob, *pad, *challenge_and_pad, *sigma, *m1, *m2, *hash;
  unsigned int m1_size, m2_size;
 
  if (challenge_size < 16 || *sig_size < n_size)
    exitSW(ERR_DATA_AUTHENTICATION_FAILURE);

  if (n_size == *sig_size) {

    memcpy(temp_buffer, challenge, challenge_size);
    crxModularExponentiation(1, n_size, exponent, (BYTE*)n, buffer, temp_buffer+challenge_size);
    temp_buffer_size = challenge_size + n_size;

    // temp_buffer contains challenge||pad||h

    pad_size          = n_size - 32;
    challenge_and_pad = temp_buffer;
    pad               = temp_buffer + challenge_size;
    hash              = temp_buffer + challenge_size + pad_size;

    SHA256(hash_prime, challenge_size + pad_size, challenge_and_pad);

    b    = pad[0];
    L    = sizeDecode(pad + 1);
    blob = pad + 3;

    if (b != 0 || L < 1 || L > n_size - 35 || memcmp(hash_prime, hash, HASH_SIZE))
      exitSW(ERR_DATA_AUTHENTICATION_FAILURE);

    memcpy(sig, blob, L);
    *sig_size = L;

  } else {
    
    sigma = sig;
    m2    = sig + n_size;

    memcpy(temp_buffer, challenge, challenge_size);
    crxModularExponentiation(1, n_size, exponent, (BYTE *)n, sigma, temp_buffer+challenge_size);
    temp_buffer_size = challenge_size + n_size;    

    // temp_buffer contains challenge||pad||h.

    pad_size          = n_size - 32;
    challenge_and_pad = temp_buffer;
    pad               = temp_buffer + challenge_size;
    memcpy(hash_, temp_buffer + challenge_size + pad_size, HASH_SIZE);


    // We want temp_buffer to contain challenge||pad||m2
    
    m2_size = *sig_size - n_size;
    memcpy(temp_buffer + challenge_size + pad_size, m2, m2_size);
    temp_buffer_size = challenge_size + pad_size + m2_size;

    // we hash

    SHA256(hash_prime, temp_buffer_size, temp_buffer);

    b       = pad[0];
    L       = sizeDecode(pad + 1);
    m1      = pad + 3;
    m1_size = pad_size - 3;

    if (b != 0 || L != n_size - 35 + m2_size || memcmp(hash_prime, hash_, HASH_SIZE))
      exitSW(ERR_DATA_AUTHENTICATION_FAILURE);
    
    memcpy(sig        , m1, m1_size);
    memcpy(sig+m1_size, m2, m2_size);
    *sig_size = m1_size + m2_size;

  }

}  
  
/************************************************************************************************************************************************
 * void checkBufferPrefix(BYTE ins, BYTE *datain, unsigned int datain_size) 
 ************************************************************************************************************************************************/

void checkBufferPrefix(BYTE ins, BYTE *datain, unsigned int datain_size) { // prefix = ins || datain
  
  unsigned int i, prefix_size;

  prefix_size = datain_size + 1;
  
  if (buffer_size < prefix_size)
    exitSW(ERR_NEED_PRIOR_DATA_AUTHENTICATION_WRT_ROOT_KEY);
  
  if (authData == 0)
    exitSW(ERR_NEED_PRIOR_DATA_AUTHENTICATION_WRT_ROOT_KEY);
  
  if (authKeyId != 0)
    exitSW(ERR_DATA_AUTHENTICATED_WRT_NON_ROOT_KEY);
  
  authData = 0;
  
  if (buffer[0] != ins || memcmp(datain, buffer+1, datain_size)) // prefix = ins || datain
    exitSW(ERR_CMD_PARAMETERS_FAILED_ROOT_AUTHENTICATION);
  
  if (buffer_size > prefix_size) {
    for (i=0; i<buffer_size; i++)
      buffer[i] = buffer[i+prefix_size];
  }
  
  buffer_size = buffer_size-prefix_size;
  
}

/************************************************************************************************************************************************
 * void checkBufferEqual(BYTE ins, BYTE *datain, unsigned int datain_size) // content = ins || datain
 ************************************************************************************************************************************************/

void checkBufferEqual(BYTE ins, BYTE *datain, unsigned int datain_size) {

  checkBufferPrefix(ins, datain, datain_size);
  if (buffer_size != 0)
    exitSW(ERR_DATA_AUTHENTICATION_FAILURE);

}

/************************************************************************************************************************************************
 * BYTE groupExists(BYTE group_id)
 ************************************************************************************************************************************************/

BYTE groupExists(BYTE group_id) {
  if (groups[group_id].modulus_size > 0)
    return 1;
  return 0;
}

/************************************************************************************************************************************************
 * BYTE generatorExists(BYTE group_id, BYTE gen_id)
 ************************************************************************************************************************************************/

BYTE generatorExists(BYTE group_id, BYTE gen_id) {
  if (groups[group_id].g_size[gen_id-1] > 0)
    return 1;
  return 0;
}

/************************************************************************************************************************************************
 * void getGroupComponent(BYTE group_id, BYTE comptype)
 ************************************************************************************************************************************************/

void getGroupComponent(BYTE group_id, BYTE comptype) {

  switch(comptype) {
  case 0:
    memcpy(buffer, groups[group_id].modulus + (MAX_BIGINT_SIZE-groups[group_id].modulus_size), groups[group_id].modulus_size);
    buffer_size = groups[group_id].modulus_size;
    break;
  case 1:
    memcpy(buffer, groups[group_id].q + (MAX_SMALLINT_SIZE-groups[group_id].q_size), groups[group_id].q_size);
    buffer_size = groups[group_id].q_size;
    break;
  case 2:
    memcpy(buffer, groups[group_id].f + (MAX_BIGINT_SIZE-groups[group_id].f_size), groups[group_id].f_size);
    buffer_size = groups[group_id].f_size;
    break;
  default:
    buffer[0] = groups[group_id].num_generators;
    buffer_size = 1;
    break;
  }

}

/************************************************************************************************************************************************
 * void getGenerator(BYTE group_id, BYTE gen_id)
 *
 * Put the generator in a MAX_BIGINT_SIZEd buffer. The significant length
 * of the generator is put in buffer_size.
 ************************************************************************************************************************************************/

void getGenerator(BYTE group_id, BYTE gen_id) {

  if (gen_id == 0 || gen_id > NUM_GEN)
    exitSW(ERR_ID_OF_GROUP_GENERATOR_OUTSIDE_OF_RANGE);

  if (!generatorExists(group_id, gen_id))
    exitSW(ERR_GENERATOR_DOES_NOT_EXIST);

  memcpy(buffer, groups[group_id].g[gen_id-1], MAX_BIGINT_SIZE);
  buffer_size = groups[group_id].g_size[gen_id-1];

}

/************************************************************************************************************************************************
 * BYTE accessCredential(BYTE pin[PIN_SIZE], BYTE credential_id)
 ************************************************************************************************************************************************/

BYTE accessCredential(BYTE *pin, BYTE credential_id) {

  if (mode != MODE_WORKING)
    exitSW(ERR_BAD_MODE ^ mode);

  if (Lc != 5)
    exitSW(ERR_INCORRECT_SIZE_OF_INCOMMING_DATA  ^ 5);

  checkPin(pin);

  if (credential_id < 1 || credential_id > NUM_CREDS)
    exitSW(ERR_CREDENTIALID_OUTSIDE_OF_RANGE);

  if (!credentials[credential_id-1].exists)
    exitSW(ERR_CREDENTIAL_DOES_NOT_EXIST);

  return credential_id;
  

}

/************************************************************************************************************************************************
 * void singleOrDoubleExpo(BYTE issuer_id, BYTE *e1, unsigned int e1_size, BYTE *e2, unsigned int e2_size)
 *
 * We assume that both e1 and e2 are buffer of size MAX_SMALLINT_SIZE and
 * that the significant bytes are right-shifted. The number of
 * significant bytes are given by e1_size and e2_size
 * respectively. 
 *
 * The least significant byte of the computation is stored in
 * buffer[MAX_BIGINT_SIZE-1]. The real size (number of significant bytes)
 * is stored in buffer_size.
 ************************************************************************************************************************************************/

void singleOrDoubleExpo(BYTE issuer_id, BYTE *e1, unsigned int e1_size, BYTE *e2, unsigned int e2_size) {

  if (issuer_id < 1 || issuer_id > NUM_ISSUERS)
    exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
      
  if (!issuers[issuer_id-1].exists)
    exitSW(ERR_ISSUER_DOES_NOT_EXIST);
      
  temp_group_id = issuers[issuer_id-1].group_id;
  temp_gen_id_1 = issuers[issuer_id-1].gen_id_1;
  temp_gen_id_2 = issuers[issuer_id-1].gen_id_2;

  if (temp_group_id >= NUM_GROUPS)
    exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

  if (!groupExists(temp_group_id))
    exitSW(ERR_GROUP_DOES_NOT_EXIST);

  temp_modulus      = groups[temp_group_id].modulus;
  temp_modulus_size = groups[temp_group_id].modulus_size;

  getGenerator(temp_group_id, temp_gen_id_1); // put the generator in a buffer of MAX_BIGINT_SIZE bytes, the real size being stored in temp_gen_1_size
  temp_gen_1_size = buffer_size;

  // 8. call ModExp(e1,m)

  if (temp_gen_1_size >= temp_modulus_size) {
    // reduce the buffer modulo m
    // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
    multosModularReduction(temp_gen_1_size, temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_gen_1_size, temp_modulus+MAX_BIGINT_SIZE-temp_modulus_size);
    temp_gen_1_size = temp_modulus_size;
  }

  // At this point we have temp_modulus_size >= temp_gen_1_size

  // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
  crxModularExponentiation(e1_size, temp_modulus_size, e1+MAX_SMALLINT_SIZE-e1_size, temp_modulus+MAX_BIGINT_SIZE-temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_modulus_size);
  buffer_size = temp_modulus_size;

  // buffer now holds g1**e1 mod m and is of size buffer_size = temp_modulus_size

  // 9. if gen != 0 ...
  if (temp_gen_id_2 != 0) {

    memcpy(temp_buffer, buffer, MAX_BIGINT_SIZE);
    temp_buffer_size = buffer_size; // = temp_modulus_size

    getGenerator(temp_group_id, temp_gen_id_2);
    temp_gen_2_size = buffer_size;

    if (temp_gen_2_size >= temp_modulus_size) {
      // reduce the buffer modulo m
      multosModularReduction(temp_gen_2_size, temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_gen_2_size, temp_modulus+MAX_BIGINT_SIZE-temp_modulus_size);
      temp_gen_2_size = temp_modulus_size;      
    }

    crxModularExponentiation(e2_size, temp_modulus_size, e2+MAX_SMALLINT_SIZE-e2_size, temp_modulus+MAX_BIGINT_SIZE-temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_modulus_size, buffer+MAX_BIGINT_SIZE-temp_modulus_size);
    buffer_size = temp_modulus_size;

    // void multosModularMultiplication (WORD modulusLength, BYTE *modulus, BYTE *block1, BYTE *block2);
    // We set the first bytes of buffer and temp_buffer to 0. This is
    // necessary for the crxModularMultiplication routine. We expect
    // temp_modulus to already have zero's on the left-most bytes.
    memset(buffer, 0, MAX_BIGINT_SIZE-temp_modulus_size);
    memset(temp_buffer, 0, MAX_BIGINT_SIZE-temp_modulus_size);
    crxModularMultiplication(temp_modulus_size,   /* modulus size */
			     temp_modulus,        /* modulus */
			     buffer,              /* buffer */
			     temp_buffer);        /* temp   */  // this overwrites buffer

  }
  
}

/************************************************************************************************************************************************
 * void accessSession(BYTE credential_id)
 ************************************************************************************************************************************************/

void accessSession(BYTE credential_id) {

  if (current_prover_id < 1 || current_prover_id > NUM_PROVERS)
    exitSW(ERR_PROVERID_OUTSIDE_OF_RANGE);

  if (!provers[current_prover_id-1].exists)
    exitSW(ERR_PROVER_DOES_NOT_EXIST);

  /* REMOVED on the 4th of october 2013
  exit = 0;
  if (provers[current_prover_id-1].cred_ids_size != 0) {
    exit = 1;
    for (i = 0; i < provers[current_prover_id-1].cred_ids_size; i++) {
      if (provers[current_prover_id-1].cred_ids[i] == credential_id)
	exit = 0;
    }
  }

  
  if (exit)
    exitSW(ERR_CREDENTIAL_OR_PSEUDO_NOT_MEMBER_PROOF_SESS);
  */
}

/************************************************************************************************************************************************
 * void singleOrDoubleResponse(BYTE issuer_id, BYTE *c, unsigned int c_size, BYTE *x, unsigned int x_size, BYTE *kx, unsigned int kx_size, BYTE *v, unsigned int v_size, BYTE *kv, unsigned int kv_size)
 *
 * The result is stored in the left-most bytes of buffer, the size of the result is stored in buffer_size.
 ************************************************************************************************************************************************/

void singleOrDoubleResponse(BYTE issuer_id, BYTE *c, unsigned int c_size, BYTE *x, unsigned int x_size, BYTE *kx, unsigned int kx_size, BYTE *v, unsigned int v_size, BYTE *kv, unsigned int kv_size) {
  
  if (issuer_id < 1 || issuer_id > NUM_ISSUERS)
    exitSW(ERR_ISSUERID_OUTSIDE_OF_RANGE);
  
  if (!issuers[issuer_id-1].exists)
    exitSW(ERR_ISSUER_DOES_NOT_EXIST);
      
  temp_group_id = issuers[issuer_id-1].group_id;
  temp_gen_id_2 = issuers[issuer_id-1].gen_id_2;

  if (temp_group_id >= NUM_GROUPS)
    exitSW(ERR_GROUPID_OUTSIDE_OF_RANGE);

  if (!groupExists(temp_group_id))
    exitSW(ERR_GROUP_DOES_NOT_EXIST);

  // fetch q = groups[temp_group_id].q;

  singleResponse(kx, kx_size, c, c_size, x, x_size, groups[temp_group_id].q, groups[temp_group_id].q_size, 0); // the result is stored in the right-most bytes of buffer[0 ... MAX_BIGINT_SIZE-1], there are buffer_size significant bytes

  if (temp_gen_id_2 != 0) {

    singleResponse(kv, kv_size, c, c_size, v, v_size, groups[temp_group_id].q, groups[temp_group_id].q_size, buffer_size);

  }

}

/************************************************************************************************************************************************
 * void singleResponse(BYTE *k, unsigned int k_size, BYTE *c, 
 *                     unsigned int c_size, BYTE *u, unsigned int u_size, 
 *                     BYTE *q, unsigned int q_size)
 *
 * We assume that all the tables are of size MAX_SMALLINT_SIZE, except for
 * c which we assume to be made of HASH_SIZE bytes. The sizes given as
 * parameters are the number of significant bytes.
 *
 * The result of the computation is stored in buffer, the most
 * significat byte being stored in buffer[offset]. The number of
 * significant bytes and offset bytes is stored in buffer_size.
 ************************************************************************************************************************************************/

void singleResponse(BYTE *k, unsigned int k_size, BYTE *c, unsigned int c_size, BYTE *u, unsigned int u_size, BYTE *q, unsigned int q_size, BYTE offset) {

  BYTE blockLength;

  // put both c and u in temp_buffer

  memset(temp_buffer, 0, 2*MAX_SMALLINT_SIZE);
  memcpy(temp_buffer+MAX_SMALLINT_SIZE-c_size, c, c_size);
  memcpy(temp_buffer+MAX_SMALLINT_SIZE, u, MAX_SMALLINT_SIZE);
  
  if (q_size != 0) {

    // compute (k - c*u) mod q

    // reduce c and u mod q and compute c*u mod q and put the result in temp_buffer
    // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
    if (c_size >= q_size)
      multosModularReduction (c_size, q_size, temp_buffer+MAX_SMALLINT_SIZE-c_size, q+MAX_SMALLINT_SIZE-q_size);
    if (u_size >= q_size)
      multosModularReduction (u_size, q_size, temp_buffer+2*MAX_SMALLINT_SIZE-u_size, q+MAX_SMALLINT_SIZE-q_size);

    // compute c * u mod q
    // Due to the fact that multosModularMultiplication stores the MSB
    // of the result on block1[0] (which is *not* how things should be
    // done), we replace this instruction with a simple
    // multiplication, followed by a modular reduction.

    // void multosBlockMultiply (const BYTE blockLength, BYTE *block1, BYTE *block2, BYTE *result)
    crxBlockMultiply127orLess (MAX_SMALLINT_SIZE-1, temp_buffer+1, temp_buffer+MAX_SMALLINT_SIZE+1, temp_buffer+2); // this puts the result in the right-most bytes of temp_buffer[0 ... 2*MAX_SMALLINT_SIZE-1]

    // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
    if (c_size + u_size >= q_size)
      multosModularReduction (c_size + u_size, q_size, temp_buffer + 2*MAX_SMALLINT_SIZE - c_size - u_size, q + MAX_SMALLINT_SIZE - q_size);    

    //temp_buffer_size = q_size; // the least significant byte of c*u mod q is in temp_buffer_size[2*MAX_SMALLINT_SIZE-1] and there are at most q_size significant bytes

    // put k in buffer. If necessary, reduce it modulo q

    memcpy(temp_buffer, k, MAX_SMALLINT_SIZE);
    if (k_size >= q_size)
      multosModularReduction (k_size, 
			      q_size, 
			      temp_buffer + MAX_SMALLINT_SIZE - k_size, /* k */
			      q           + MAX_SMALLINT_SIZE - q_size  /* q */
			      );

    // we want to compute k - cu mod q, we already have k mod q in temp_buffer[0..MAX_SMALLINT_SIZE-1] and cu mod q in temp_buffer[MAX_SMALLINT_SIZE..2*MAX_SMALLINT_SIZE-1]

    /* if k mod q < cu mod q, replace k by k + q */
    if(memcmp(temp_buffer, temp_buffer+MAX_SMALLINT_SIZE, MAX_SMALLINT_SIZE) < 0) {
      // we have that (k mod q) < (c*u mod q)
      // void multosBlockAdd (const BYTE blockLength, BYTE *block1, BYTE *block2, const BYTE *result);
      multosBlockAdd (MAX_SMALLINT_SIZE, temp_buffer, q, temp_buffer);
    }

    // void multosBlockSubract (const BYTE blockLength, BYTE *block1, BYTE *block2, const BYTE *result);
    //multosBlockSubract (q_size, buffer+MAX_SMALLINT_SIZE-q_size, temp_buffer+MAX_SMALLINT_SIZE-q_size, buffer+MAX_SMALLINT_SIZE-q_size);
    multosBlockSubract (MAX_SMALLINT_SIZE, 
			temp_buffer, 
			temp_buffer+MAX_SMALLINT_SIZE, 
			temp_buffer);

    memcpy(buffer+offset, temp_buffer+MAX_SMALLINT_SIZE-q_size, q_size);
    buffer_size = q_size+offset;

  } else {

    // compute k - c*u over the integers

    // make sure that c*u fits a big integer

    if (c_size + u_size > MAX_SMALLINT_SIZE)
      exitSW(ERR_INTEGER_EXCEEDS_MAXINTSIZE);

    // compute c*u, put it in temp_buffer[0 ... MAX_SMALLINT_SIZE-1]
    // void multosBlockMultiply (const BYTE blockLength, BYTE *block1, BYTE *block2, BYTE *result);
    crxBlockMultiply127orLess (MAX_SMALLINT_SIZE-1, temp_buffer+1, temp_buffer+MAX_SMALLINT_SIZE+1, temp_buffer+2); // this puts the result in the right-most bytes of temp_buffer[0 ... 2*MAX_SMALLINT_SIZE-1]
    
    // since c_size + u_size <= MAX_SMALLINT_SIZE, we can put the result back in the MAX_SMALLINT_SIZE first bytes of temp_buffer

    memcpy(temp_buffer, temp_buffer+MAX_SMALLINT_SIZE, MAX_SMALLINT_SIZE);

    // put k 

    memcpy(temp_buffer+MAX_SMALLINT_SIZE, k, MAX_SMALLINT_SIZE);
    
    // compute (temp_buffer+MAX_SMALLINT_SIZE) - temp_buffer (i.e., k - cu), both buffers being made of MAX_INT_BYTES bytes

    multosBlockSubract(MAX_SMALLINT_SIZE, temp_buffer+MAX_SMALLINT_SIZE, temp_buffer, temp_buffer);

    buffer_size = k_size+offset;
    memcpy(buffer+offset, temp_buffer+MAX_SMALLINT_SIZE-k_size, k_size);

  }

}

/************************************************************************************************************************************************
 * void scopeExclusiveGenerator()
 *
 * Put the result in the right-most bytes of buffer[0 ... MAX_BIGINT_SIZE-1]. 
 * The significant length of the result is stored in buffer_size.
 ************************************************************************************************************************************************/

void scopeExclusiveGenerator(BYTE *scope, unsigned int scope_size, BYTE *m, unsigned int m_size, BYTE *f, unsigned int f_size) {
  
  memset(buffer, 0, MAX_BIGINT_SIZE); // alloc for buffer: MAX_APDU_INPUT_DATA_SIZE bytes
  SHA256(buffer+MAX_BIGINT_SIZE-HASH_SIZE, scope_size, scope);
  buffer_size = HASH_SIZE;
  
  if (f_size != 0) {
    
    // void multosModularReduction (WORD operandLength, WORD modulusLength, BYTE *operand, BYTE *modulus);
    if (buffer_size >= m_size) {
      multosModularReduction (buffer_size, m_size, buffer+MAX_BIGINT_SIZE-buffer_size, m+MAX_BIGINT_SIZE-m_size);
      buffer_size = m_size;
    }
    
    // void crxModularExponentiation (WORD exponentLength, WORD modulusLength, BYTE *exponent, BYTE *modulus, BYTE *input, BYTE *output);
    crxModularExponentiation(f_size, m_size, f+MAX_BIGINT_SIZE-f_size, m+MAX_BIGINT_SIZE-m_size, buffer+MAX_BIGINT_SIZE-m_size, buffer+MAX_BIGINT_SIZE-m_size);
    buffer_size = m_size;
    
  }
  
}


/************************************************************************************************************************************************
 * BYTE* accessURI(BYTE *datain, unsigned int Lc)
 ************************************************************************************************************************************************/

BYTE* accessURI(BYTE *datain, unsigned int Lc) {

  BYTE *uri;
  BYTE uri_size;
  
  if (mode != MODE_ROOT && mode != MODE_WORKING)
    exitSW(ERR_BAD_MODE ^ mode);
  
  if (Lc < 5)
    exitSW(ERR_INCORRECT_MIN_SIZE_OF_INCOMMING_DATA ^ 5);

  checkPin(datain);

  uri = datain + PIN_SIZE;
  uri_size = Lc - PIN_SIZE;

  if (uri_size > MAX_URI_SIZE)
    exitSW(ERR_URI_TOO_LARGE);

  return uri;

}

/************************************************************************************************************************************************
 * void getBlobstoreInformations(unsigned int* first_available_index, unsigned int *blobcount, unsigned int *uri_index, unsigned char *uri, BYTE uri_size)
 *
 * first_available_index will point to the first free blob
 * location. If there is no more space available, then
 * first_available_index = MAX_NUMBER_OF_BLOBS.
 *
 * When uri != null (in which case, uri_size must be specified),
 * uri_index will be initialized to the index of the blob with this
 * uri. If no such uri exists, then uri_index = MAX_NUMBER_OF_BLOBS.
 ************************************************************************************************************************************************/

void getBlobstoreInformations(unsigned int* first_available_index, unsigned int *blobcount, unsigned int *uri_index, unsigned char *uri, BYTE uri_size) {

  unsigned int i;

  *first_available_index = MAX_NUMBER_OF_BLOBS;
  *blobcount = 0;
  *uri_index = MAX_NUMBER_OF_BLOBS;

  for (i = 0; i < MAX_NUMBER_OF_BLOBS; i++) {
    if (blob_catalog[i].exists) {

      (*blobcount)++;

      if (uri != NULL && uri_size == blob_catalog[i].uri_size && memcmp(uri, blob_catalog[i].uri, uri_size) == 0) {
        *uri_index = i;
      }
      
    } else {

      if (*first_available_index == MAX_NUMBER_OF_BLOBS)
	*first_available_index = i;

    }
  }

}

/************************************************************************************************************************************************
 * void encrypt(BYTE *password, BYTE label) 
 *
 * Encrypt the buffer of size buffer_size under the given
 * password. Store the result at the begining of buffer.
 ************************************************************************************************************************************************/

void encrypt(BYTE *password, BYTE label) {

  temp_buffer_size = 0;
  memcpy(temp_buffer+temp_buffer_size, master_backup_key, MASTER_BACKUP_KEY_SIZE);
  temp_buffer_size += MASTER_BACKUP_KEY_SIZE;
  memcpy(temp_buffer+temp_buffer_size, password, PASSWORD_SIZE);
  temp_buffer_size += PASSWORD_SIZE;
  memcpy(temp_buffer+temp_buffer_size, &label, 1);
  temp_buffer_size += 1;

  SHA256(temp_buffer, temp_buffer_size, temp_buffer);
  temp_buffer_size = 16;

  // temp_buffer contains K (16 bytes)

  // encryption of buffer under key = temp_buffer (first 16 bytes), with this device id

  // create pad, make it start at temp_buffer+16
  pad_size = 0;

  memset(temp_buffer+16+pad_size, 0, 4);
  pad_size += 4;

  memcpy(temp_buffer+16+pad_size, &buffer_size, 2);
  pad_size += 2;

  memcpy(temp_buffer+16+pad_size, &device_id, ID_SIZE);
  pad_size += ID_SIZE;

#ifdef TEST_MODE
  memset(temp_buffer+16+pad_size, 0xaa, 8);
#else
  getRandomBytes(temp_buffer+16+pad_size, 8);
#endif
  pad_size += 8;

  // temp_buffer contains K (16 bytes) || pad (16 bytes)

  // encrypt with AES128
  // void multosAESECBEncipher (BYTE *plainText, BYTE *cipherText, BYTE keyLength, BYTE *key);

  multosAESECBEncipher(temp_buffer+16, temp_buffer+32, 16, temp_buffer);

  // temp_buffer contains K (16 bytes) || pad (16 bytes) || t (16 bytes)

  // SHA256(digest, plaintext_length, plaintext)
  SHA256(temp_buffer+48, 32, temp_buffer);

  // temp_buffer contains K (16 bytes) || pad (16 bytes) || t (16 bytes) || k (16 bytes) || c0 (16 bytes) (ok !)

  // right-pad buffer with 0x00 bytes until its size is a multiple of 16
  if ((buffer_size & 0xf) != 0) {  // mod 16
    memset(buffer+buffer_size, 0, 16-(buffer_size & 0xf));
    buffer_size += (16-(buffer_size & 0xf));
  }
  
  d = buffer_size / 16;
  
  // buffer = buffer1 || ... || buffer_d where size(buffer_i) = 16 bytes

  // crxAESEncryptCBC(BYTE *ciphertext, const BYTE *iv, const BYTE *plaintext, WORD plaintext_size, const BYTE *key)
  crxAESEncryptCBC(temp_buffer+80, temp_buffer+64, buffer, buffer_size, temp_buffer+48);

  // temp_buffer contains K (16 bytes) || pad (16 bytes) || t (16 bytes) || k (16 bytes) || c0 (16 bytes) || c_1 (16 bytes) || ... || c_d (16 bytes)

  buffer_size = 0;
  memcpy(buffer + buffer_size, temp_buffer+32, 16); // copy 't'
  buffer_size += 16;
  memcpy(buffer + buffer_size, temp_buffer+80, 16*d); // copy c_1 || ... || c_d
  buffer_size += (16*d);
  
}

/************************************************************************************************************************************************
 * void decrypt(BYTE *device_id_prim, BYTE *password, BYTE label) 
 *
 * Decrypt the buffer of size buffer_size under the given
 * password. Store the result at the begining of buffer.
 *
 * Set device_id_prim to the value found during the decryption
 * process.
 ************************************************************************************************************************************************/

void decrypt(BYTE *device_id_prim, BYTE *password, BYTE label) {

  unsigned int L;

  temp_buffer_size = 0;
  memcpy(temp_buffer+temp_buffer_size, master_backup_key, MASTER_BACKUP_KEY_SIZE);
  temp_buffer_size += MASTER_BACKUP_KEY_SIZE;
  memcpy(temp_buffer+temp_buffer_size, password, PASSWORD_SIZE);
  temp_buffer_size += PASSWORD_SIZE;
  memcpy(temp_buffer+temp_buffer_size, &label, 1);
  temp_buffer_size += 1;

  SHA256(temp_buffer, temp_buffer_size, temp_buffer);
  temp_buffer_size = 16;

  // temp_buffer contains : K (16 bytes)

  if ((buffer_size & 0xf) != 0)
    exitSW(ERR_INVALID_BACKUP_ARCHIVE);

  d = (buffer_size - 16)/16;

  // buffer contains : t (16 bytes) || c_1 (16 bytes) || ... || c_d (16 bytes)

  memcpy(temp_buffer+32, buffer, 16);

  // temp_buffer contains : K (16 bytes) || nothing (16 bytes) || t (16 bytes)

  // void multosAESECBDecipher (BYTE *cipherText, BYTE *plainText, BYTE keyLength, BYTE *key);
  multosAESECBDecipher (temp_buffer+32, temp_buffer+16, 16, temp_buffer);

  // temp_buffer should contain : K (16 bytes) || pad (16 bytes) || t (16 bytes)
  // where pad = 0x00 0x00 0x00 0x00 || L (2 bytes) || deviceId' (2 bytes) || z (8 bytes)

  memcpy(device_id_prim, temp_buffer+22, 2);

  memset(mem_session.small_buffer, 0, 4);
  if (memcmp(temp_buffer+16, mem_session.small_buffer, 4) != 0)
    exitSW(ERR_INVALID_BACKUP_ARCHIVE);

  memcpy(&L, temp_buffer+20, 2);

  if (L < 16*(d-1)+1 || L > 16*d)
    exitSW(ERR_INVALID_BACKUP_ARCHIVE);

  // SHA256(digest, plaintext_length, plaintext)
  SHA256(temp_buffer+48, 32, temp_buffer);

  // temp_buffer contains : K (16 bytes) || pad (16 bytes) || t (16 bytes) || k (16 bytes) || c_0 (16 bytes)

  memcpy(temp_buffer+80, buffer+16, 16*d);

  // temp_buffer contains : K (16 bytes) || pad (16 bytes) || t (16 bytes) || k (16 bytes) || c_0 (16 bytes) || c_1 (16 bytes) || ... || c_d (16 bytes)

  // crxAESDecryptCBC(BYTE *plaintext, const BYTE *iv, const BYTE *ciphertext, WORD ciphertext_size, const BYTE *key)
  crxAESDecryptCBC(buffer, temp_buffer+64, temp_buffer+80, 16*d, temp_buffer+48);

  // buffer contains : data_1 (16 bytes) || ... || data_d (16 bytes)

  memset(mem_session.small_buffer, 0, 16*d - L);
  if (memcmp(buffer+L, mem_session.small_buffer, 16*d - L) != 0)
    exitSW(ERR_INVALID_BACKUP_ARCHIVE);

  buffer_size = L;

}

/************************************************************************************************************************************************
 * void print(BYTE *s, unsigned int size)
 *
 ************************************************************************************************************************************************/

void print(void *s, unsigned int size) {

  memcpy(apdu_data.dataout, (BYTE *)s, size);

  exitLa(size);

}

/************************************************************************
 * void output_large_data()
 *
 * This routine expects the global variable *remaining_position to
 * point at the begining of the data to be sent. The global variable
 * remaining_size should specify the total length of the data to be
 * sent (which can be larger than 255).
 ************************************************************************/

void output_large_data(void) {
  
  WORD output_size = 0;

  if (remaining_size == 0) {
    multosExit();
  }

  output_size = MIN(remaining_size, MAX_APDU_OUTPUT_DATA_SIZE);

  memcpy(apdu_data.dataout, remaining_position, output_size);

  remaining_position = remaining_position + output_size;
  remaining_size -= output_size;

  switch (remaining_size) {
  case 0:
    exitLa(output_size);
    break;
  case 1:
    exitSWLa(0x6101, 0xFF);
    break;
  case 2:
    exitSWLa(0x6102, 0xFF);
    break;
  case 0x03:
    exitSWLa(0x6103, 0xFF);
    break;
  case 0x04:
    exitSWLa(0x6104, 0xFF);
    break;
  case 0x05:
    exitSWLa(0x6105, 0xFF);
    break;
  case 0x06:
    exitSWLa(0x6106, 0xFF);
    break;
  case 0x07:
    exitSWLa(0x6107, 0xFF);
    break;
  case 0x08:
    exitSWLa(0x6108, 0xFF);
    break;
  case 0x09:
    exitSWLa(0x6109, 0xFF);
    break;
  case 0x0a:
    exitSWLa(0x610a, 0xFF);
    break;
  case 0x0b:
    exitSWLa(0x610b, 0xFF);
    break;
  case 0x0c:
    exitSWLa(0x610c, 0xFF);
    break;
  case 0x0d:
    exitSWLa(0x610d, 0xFF);
    break;
  case 0x0e:
    exitSWLa(0x610e, 0xFF);
    break;
  case 0x0f:
    exitSWLa(0x610f, 0xFF);
    break;
  case 0x10:
    exitSWLa(0x6110, 0xFF);
    break;
  case 0x11:
    exitSWLa(0x6111, 0xFF);
    break;
  case 0x12:
    exitSWLa(0x6112, 0xFF);
    break;
  case 0x13:
    exitSWLa(0x6113, 0xFF);
    break;
  case 0x14:
    exitSWLa(0x6114, 0xFF);
    break;
  case 0x15:
    exitSWLa(0x6115, 0xFF);
    break;
  case 0x16:
    exitSWLa(0x6116, 0xFF);
    break;
  case 0x17:
    exitSWLa(0x6117, 0xFF);
    break;
  case 0x18:
    exitSWLa(0x6118, 0xFF);
    break;
  case 0x19:
    exitSWLa(0x6119, 0xFF);
    break;
  case 0x1a:
    exitSWLa(0x611a, 0xFF);
    break;
  case 0x1b:
    exitSWLa(0x611b, 0xFF);
    break;
  case 0x1c:
    exitSWLa(0x611c, 0xFF);
    break;
  case 0x1d:
    exitSWLa(0x611d, 0xFF);
    break;
  case 0x1e:
    exitSWLa(0x611e, 0xFF);
    break;
  case 0x1f:
    exitSWLa(0x611f, 0xFF);
    break;
  case 0x20:
    exitSWLa(0x6120, 0xFF);
    break;
  case 0x21:
    exitSWLa(0x6121, 0xFF);
    break;
  case 0x22:
    exitSWLa(0x6122, 0xFF);
    break;
  case 0x23:
    exitSWLa(0x6123, 0xFF);
    break;
  case 0x24:
    exitSWLa(0x6124, 0xFF);
    break;
  case 0x25:
    exitSWLa(0x6125, 0xFF);
    break;
  case 0x26:
    exitSWLa(0x6126, 0xFF);
    break;
  case 0x27:
    exitSWLa(0x6127, 0xFF);
    break;
  case 0x28:
    exitSWLa(0x6128, 0xFF);
    break;
  case 0x29:
    exitSWLa(0x6129, 0xFF);
    break;
  case 0x2a:
    exitSWLa(0x612a, 0xFF);
    break;
  case 0x2b:
    exitSWLa(0x612b, 0xFF);
    break;
  case 0x2c:
    exitSWLa(0x612c, 0xFF);
    break;
  case 0x2d:
    exitSWLa(0x612d, 0xFF);
    break;
  case 0x2e:
    exitSWLa(0x612e, 0xFF);
    break;
  case 0x2f:
    exitSWLa(0x612f, 0xFF);
    break;
  case 0x30:
    exitSWLa(0x6130, 0xFF);
    break;
  case 0x31:
    exitSWLa(0x6131, 0xFF);
    break;
  case 0x32:
    exitSWLa(0x6132, 0xFF);
    break;
  case 0x33:
    exitSWLa(0x6133, 0xFF);
    break;
  case 0x34:
    exitSWLa(0x6134, 0xFF);
    break;
  case 0x35:
    exitSWLa(0x6135, 0xFF);
    break;
  case 0x36:
    exitSWLa(0x6136, 0xFF);
    break;
  case 0x37:
    exitSWLa(0x6137, 0xFF);
    break;
  case 0x38:
    exitSWLa(0x6138, 0xFF);
    break;
  case 0x39:
    exitSWLa(0x6139, 0xFF);
    break;
  case 0x3a:
    exitSWLa(0x613a, 0xFF);
    break;
  case 0x3b:
    exitSWLa(0x613b, 0xFF);
    break;
  case 0x3c:
    exitSWLa(0x613c, 0xFF);
    break;
  case 0x3d:
    exitSWLa(0x613d, 0xFF);
    break;
  case 0x3e:
    exitSWLa(0x613e, 0xFF);
    break;
  case 0x3f:
    exitSWLa(0x613f, 0xFF);
    break;
  case 0x40:
    exitSWLa(0x6140, 0xFF);
    break;
  case 0x41:
    exitSWLa(0x6141, 0xFF);
    break;
  case 0x42:
    exitSWLa(0x6142, 0xFF);
    break;
  case 0x43:
    exitSWLa(0x6143, 0xFF);
    break;
  case 0x44:
    exitSWLa(0x6144, 0xFF);
    break;
  case 0x45:
    exitSWLa(0x6145, 0xFF);
    break;
  case 0x46:
    exitSWLa(0x6146, 0xFF);
    break;
  case 0x47:
    exitSWLa(0x6147, 0xFF);
    break;
  case 0x48:
    exitSWLa(0x6148, 0xFF);
    break;
  case 0x49:
    exitSWLa(0x6149, 0xFF);
    break;
  case 0x4a:
    exitSWLa(0x614a, 0xFF);
    break;
  case 0x4b:
    exitSWLa(0x614b, 0xFF);
    break;
  case 0x4c:
    exitSWLa(0x614c, 0xFF);
    break;
  case 0x4d:
    exitSWLa(0x614d, 0xFF);
    break;
  case 0x4e:
    exitSWLa(0x614e, 0xFF);
    break;
  case 0x4f:
    exitSWLa(0x614f, 0xFF);
    break;
  case 0x50:
    exitSWLa(0x6150, 0xFF);
    break;
  case 0x51:
    exitSWLa(0x6151, 0xFF);
    break;
  case 0x52:
    exitSWLa(0x6152, 0xFF);
    break;
  case 0x53:
    exitSWLa(0x6153, 0xFF);
    break;
  case 0x54:
    exitSWLa(0x6154, 0xFF);
    break;
  case 0x55:
    exitSWLa(0x6155, 0xFF);
    break;
  case 0x56:
    exitSWLa(0x6156, 0xFF);
    break;
  case 0x57:
    exitSWLa(0x6157, 0xFF);
    break;
  case 0x58:
    exitSWLa(0x6158, 0xFF);
    break;
  case 0x59:
    exitSWLa(0x6159, 0xFF);
    break;
  case 0x5a:
    exitSWLa(0x615a, 0xFF);
    break;
  case 0x5b:
    exitSWLa(0x615b, 0xFF);
    break;
  case 0x5c:
    exitSWLa(0x615c, 0xFF);
    break;
  case 0x5d:
    exitSWLa(0x615d, 0xFF);
    break;
  case 0x5e:
    exitSWLa(0x615e, 0xFF);
    break;
  case 0x5f:
    exitSWLa(0x615f, 0xFF);
    break;
  case 0x60:
    exitSWLa(0x6160, 0xFF);
    break;
  case 0x61:
    exitSWLa(0x6161, 0xFF);
    break;
  case 0x62:
    exitSWLa(0x6162, 0xFF);
    break;
  case 0x63:
    exitSWLa(0x6163, 0xFF);
    break;
  case 0x64:
    exitSWLa(0x6164, 0xFF);
    break;
  case 0x65:
    exitSWLa(0x6165, 0xFF);
    break;
  case 0x66:
    exitSWLa(0x6166, 0xFF);
    break;
  case 0x67:
    exitSWLa(0x6167, 0xFF);
    break;
  case 0x68:
    exitSWLa(0x6168, 0xFF);
    break;
  case 0x69:
    exitSWLa(0x6169, 0xFF);
    break;
  case 0x6a:
    exitSWLa(0x616a, 0xFF);
    break;
  case 0x6b:
    exitSWLa(0x616b, 0xFF);
    break;
  case 0x6c:
    exitSWLa(0x616c, 0xFF);
    break;
  case 0x6d:
    exitSWLa(0x616d, 0xFF);
    break;
  case 0x6e:
    exitSWLa(0x616e, 0xFF);
    break;
  case 0x6f:
    exitSWLa(0x616f, 0xFF);
    break;
  case 0x70:
    exitSWLa(0x6170, 0xFF);
    break;
  case 0x71:
    exitSWLa(0x6171, 0xFF);
    break;
  case 0x72:
    exitSWLa(0x6172, 0xFF);
    break;
  case 0x73:
    exitSWLa(0x6173, 0xFF);
    break;
  case 0x74:
    exitSWLa(0x6174, 0xFF);
    break;
  case 0x75:
    exitSWLa(0x6175, 0xFF);
    break;
  case 0x76:
    exitSWLa(0x6176, 0xFF);
    break;
  case 0x77:
    exitSWLa(0x6177, 0xFF);
    break;
  case 0x78:
    exitSWLa(0x6178, 0xFF);
    break;
  case 0x79:
    exitSWLa(0x6179, 0xFF);
    break;
  case 0x7a:
    exitSWLa(0x617a, 0xFF);
    break;
  case 0x7b:
    exitSWLa(0x617b, 0xFF);
    break;
  case 0x7c:
    exitSWLa(0x617c, 0xFF);
    break;
  case 0x7d:
    exitSWLa(0x617d, 0xFF);
    break;
  case 0x7e:
    exitSWLa(0x617e, 0xFF);
    break;
  case 0x7f:
    exitSWLa(0x617f, 0xFF);
    break;
  case 0x80:
    exitSWLa(0x6180, 0xFF);
    break;
  case 0x81:
    exitSWLa(0x6181, 0xFF);
    break;
  case 0x82:
    exitSWLa(0x6182, 0xFF);
    break;
  case 0x83:
    exitSWLa(0x6183, 0xFF);
    break;
  case 0x84:
    exitSWLa(0x6184, 0xFF);
    break;
  case 0x85:
    exitSWLa(0x6185, 0xFF);
    break;
  case 0x86:
    exitSWLa(0x6186, 0xFF);
    break;
  case 0x87:
    exitSWLa(0x6187, 0xFF);
    break;
  case 0x88:
    exitSWLa(0x6188, 0xFF);
    break;
  case 0x89:
    exitSWLa(0x6189, 0xFF);
    break;
  case 0x8a:
    exitSWLa(0x618a, 0xFF);
    break;
  case 0x8b:
    exitSWLa(0x618b, 0xFF);
    break;
  case 0x8c:
    exitSWLa(0x618c, 0xFF);
    break;
  case 0x8d:
    exitSWLa(0x618d, 0xFF);
    break;
  case 0x8e:
    exitSWLa(0x618e, 0xFF);
    break;
  case 0x8f:
    exitSWLa(0x618f, 0xFF);
    break;
  case 0x90:
    exitSWLa(0x6190, 0xFF);
    break;
  case 0x91:
    exitSWLa(0x6191, 0xFF);
    break;
  case 0x92:
    exitSWLa(0x6192, 0xFF);
    break;
  case 0x93:
    exitSWLa(0x6193, 0xFF);
    break;
  case 0x94:
    exitSWLa(0x6194, 0xFF);
    break;
  case 0x95:
    exitSWLa(0x6195, 0xFF);
    break;
  case 0x96:
    exitSWLa(0x6196, 0xFF);
    break;
  case 0x97:
    exitSWLa(0x6197, 0xFF);
    break;
  case 0x98:
    exitSWLa(0x6198, 0xFF);
    break;
  case 0x99:
    exitSWLa(0x6199, 0xFF);
    break;
  case 0x9a:
    exitSWLa(0x619a, 0xFF);
    break;
  case 0x9b:
    exitSWLa(0x619b, 0xFF);
    break;
  case 0x9c:
    exitSWLa(0x619c, 0xFF);
    break;
  case 0x9d:
    exitSWLa(0x619d, 0xFF);
    break;
  case 0x9e:
    exitSWLa(0x619e, 0xFF);
    break;
  case 0x9f:
    exitSWLa(0x619f, 0xFF);
    break;
  case 0xa0:
    exitSWLa(0x61a0, 0xFF);
    break;
  case 0xa1:
    exitSWLa(0x61a1, 0xFF);
    break;
  case 0xa2:
    exitSWLa(0x61a2, 0xFF);
    break;
  case 0xa3:
    exitSWLa(0x61a3, 0xFF);
    break;
  case 0xa4:
    exitSWLa(0x61a4, 0xFF);
    break;
  case 0xa5:
    exitSWLa(0x61a5, 0xFF);
    break;
  case 0xa6:
    exitSWLa(0x61a6, 0xFF);
    break;
  case 0xa7:
    exitSWLa(0x61a7, 0xFF);
    break;
  case 0xa8:
    exitSWLa(0x61a8, 0xFF);
    break;
  case 0xa9:
    exitSWLa(0x61a9, 0xFF);
    break;
  case 0xaa:
    exitSWLa(0x61aa, 0xFF);
    break;
  case 0xab:
    exitSWLa(0x61ab, 0xFF);
    break;
  case 0xac:
    exitSWLa(0x61ac, 0xFF);
    break;
  case 0xad:
    exitSWLa(0x61ad, 0xFF);
    break;
  case 0xae:
    exitSWLa(0x61ae, 0xFF);
    break;
  case 0xaf:
    exitSWLa(0x61af, 0xFF);
    break;
  case 0xb0:
    exitSWLa(0x61b0, 0xFF);
    break;
  case 0xb1:
    exitSWLa(0x61b1, 0xFF);
    break;
  case 0xb2:
    exitSWLa(0x61b2, 0xFF);
    break;
  case 0xb3:
    exitSWLa(0x61b3, 0xFF);
    break;
  case 0xb4:
    exitSWLa(0x61b4, 0xFF);
    break;
  case 0xb5:
    exitSWLa(0x61b5, 0xFF);
    break;
  case 0xb6:
    exitSWLa(0x61b6, 0xFF);
    break;
  case 0xb7:
    exitSWLa(0x61b7, 0xFF);
    break;
  case 0xb8:
    exitSWLa(0x61b8, 0xFF);
    break;
  case 0xb9:
    exitSWLa(0x61b9, 0xFF);
    break;
  case 0xba:
    exitSWLa(0x61ba, 0xFF);
    break;
  case 0xbb:
    exitSWLa(0x61bb, 0xFF);
    break;
  case 0xbc:
    exitSWLa(0x61bc, 0xFF);
    break;
  case 0xbd:
    exitSWLa(0x61bd, 0xFF);
    break;
  case 0xbe:
    exitSWLa(0x61be, 0xFF);
    break;
  case 0xbf:
    exitSWLa(0x61bf, 0xFF);
    break;
  case 0xc0:
    exitSWLa(0x61c0, 0xFF);
    break;
  case 0xc1:
    exitSWLa(0x61c1, 0xFF);
    break;
  case 0xc2:
    exitSWLa(0x61c2, 0xFF);
    break;
  case 0xc3:
    exitSWLa(0x61c3, 0xFF);
    break;
  case 0xc4:
    exitSWLa(0x61c4, 0xFF);
    break;
  case 0xc5:
    exitSWLa(0x61c5, 0xFF);
    break;
  case 0xc6:
    exitSWLa(0x61c6, 0xFF);
    break;
  case 0xc7:
    exitSWLa(0x61c7, 0xFF);
    break;
  case 0xc8:
    exitSWLa(0x61c8, 0xFF);
    break;
  case 0xc9:
    exitSWLa(0x61c9, 0xFF);
    break;
  case 0xca:
    exitSWLa(0x61ca, 0xFF);
    break;
  case 0xcb:
    exitSWLa(0x61cb, 0xFF);
    break;
  case 0xcc:
    exitSWLa(0x61cc, 0xFF);
    break;
  case 0xcd:
    exitSWLa(0x61cd, 0xFF);
    break;
  case 0xce:
    exitSWLa(0x61ce, 0xFF);
    break;
  case 0xcf:
    exitSWLa(0x61cf, 0xFF);
    break;
  case 0xd0:
    exitSWLa(0x61d0, 0xFF);
    break;
  case 0xd1:
    exitSWLa(0x61d1, 0xFF);
    break;
  case 0xd2:
    exitSWLa(0x61d2, 0xFF);
    break;
  case 0xd3:
    exitSWLa(0x61d3, 0xFF);
    break;
  case 0xd4:
    exitSWLa(0x61d4, 0xFF);
    break;
  case 0xd5:
    exitSWLa(0x61d5, 0xFF);
    break;
  case 0xd6:
    exitSWLa(0x61d6, 0xFF);
    break;
  case 0xd7:
    exitSWLa(0x61d7, 0xFF);
    break;
  case 0xd8:
    exitSWLa(0x61d8, 0xFF);
    break;
  case 0xd9:
    exitSWLa(0x61d9, 0xFF);
    break;
  case 0xda:
    exitSWLa(0x61da, 0xFF);
    break;
  case 0xdb:
    exitSWLa(0x61db, 0xFF);
    break;
  case 0xdc:
    exitSWLa(0x61dc, 0xFF);
    break;
  case 0xdd:
    exitSWLa(0x61dd, 0xFF);
    break;
  case 0xde:
    exitSWLa(0x61de, 0xFF);
    break;
  case 0xdf:
    exitSWLa(0x61df, 0xFF);
    break;
  case 0xe0:
    exitSWLa(0x61e0, 0xFF);
    break;
  case 0xe1:
    exitSWLa(0x61e1, 0xFF);
    break;
  case 0xe2:
    exitSWLa(0x61e2, 0xFF);
    break;
  case 0xe3:
    exitSWLa(0x61e3, 0xFF);
    break;
  case 0xe4:
    exitSWLa(0x61e4, 0xFF);
    break;
  case 0xe5:
    exitSWLa(0x61e5, 0xFF);
    break;
  case 0xe6:
    exitSWLa(0x61e6, 0xFF);
    break;
  case 0xe7:
    exitSWLa(0x61e7, 0xFF);
    break;
  case 0xe8:
    exitSWLa(0x61e8, 0xFF);
    break;
  case 0xe9:
    exitSWLa(0x61e9, 0xFF);
    break;
  case 0xea:
    exitSWLa(0x61ea, 0xFF);
    break;
  case 0xeb:
    exitSWLa(0x61eb, 0xFF);
    break;
  case 0xec:
    exitSWLa(0x61ec, 0xFF);
    break;
  case 0xed:
    exitSWLa(0x61ed, 0xFF);
    break;
  case 0xee:
    exitSWLa(0x61ee, 0xFF);
    break;
  case 0xef:
    exitSWLa(0x61ef, 0xFF);
    break;
  case 0xf0:
    exitSWLa(0x61f0, 0xFF);
    break;
  case 0xf1:
    exitSWLa(0x61f1, 0xFF);
    break;
  case 0xf2:
    exitSWLa(0x61f2, 0xFF);
    break;
  case 0xf3:
    exitSWLa(0x61f3, 0xFF);
    break;
  case 0xf4:
    exitSWLa(0x61f4, 0xFF);
    break;
  case 0xf5:
    exitSWLa(0x61f5, 0xFF);
    break;
  case 0xf6:
    exitSWLa(0x61f6, 0xFF);
    break;
  case 0xf7:
    exitSWLa(0x61f7, 0xFF);
    break;
  case 0xf8:
    exitSWLa(0x61f8, 0xFF);
    break;
  case 0xf9:
    exitSWLa(0x61f9, 0xFF);
    break;
  case 0xfa:
    exitSWLa(0x61fa, 0xFF);
    break;
  case 0xfb:
    exitSWLa(0x61fb, 0xFF);
    break;
  case 0xfc:
    exitSWLa(0x61fc, 0xFF);
    break;
  case 0xfd:
    exitSWLa(0x61fd, 0xFF);
    break;
  case 0xfe:
    exitSWLa(0x61fe, 0xFF);
    break;
  default:
    exitSWLa(0x61ff, 0xFF);
    break;
  }

}

/******************************************************************************
 * void segmentToStaticHigh(void *high_addr, const void *low_addr, size_t size)
 *
 * This routine copies the content of the buffer located in the normal
 * address space (ram of e2), starting at address low_addr, to the the
 * buffer (possibly) located in the higher part of the static memory,
 * starting at address high_addr. If high_addr+size < 0x8000, then
 * this routine can be replace by a memcpy.
 ******************************************************************************/

void segmentToStaticHigh(void *high_addr, const void *low_addr, size_t size)
{

  BYTE high_addr_32bits[4] = {0x00, 0x00, 0x00, 0x00};

  // compute high_addr_32bits
  memcpy(high_addr_32bits+2, &high_addr, 2);

  // call MEMORY COPY ADDITION STATIC (non atomic)
  __push ((__typechk(WORD, size))); // size of the data to copy
  __push (__BLOCKCAST(4)(__typechk(BYTE *, high_addr_32bits))); // push the static offset
  __push ((__typechk(WORD, (WORD)(low_addr)))); // address of the data in the low memory
  __code (__PRIM, 0xDD, 0x80);

}

/******************************************************************************
 * void staticHighToSegment(void *low_addr, const void *high_addr, size_t size)
 *
 * This routine copies the content of the buffer (possibly) located in
 * the high address space, starting at address high_addr, to the the
 * buffer located in the lower part of the memory (ram of e2),
 * starting at address low_addr.
 ******************************************************************************/

void staticHighToSegment(void *low_addr, const void *high_addr, size_t size)
{

  BYTE high_addr_32bits[4] = {0x00, 0x00, 0x00, 0x00};

  // compute high_addr_32bits
  memcpy(high_addr_32bits+2, &high_addr, 2);

  // call MEMORY COPY ADDITION STATIC (non atomic)
  __push ((__typechk(WORD, size))); // size of the data to copy
  __push ((__typechk(WORD, (WORD)(low_addr)))); // address of the data in the low memory
  __push (__BLOCKCAST(4)(__typechk(BYTE *, high_addr_32bits))); // push the static offset
  __code (__PRIM, 0xDD, 0x81);

}
