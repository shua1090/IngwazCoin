package Ingwaz.BlockChain;

import org.iq80.leveldb.DBException;

public class AddressDNEException extends DBException {
    public AddressDNEException(String the_address_does_not_exist) {
        super(the_address_does_not_exist);
    }
}
