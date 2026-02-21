package com.example.webauthn;

import com.example.security.fido.yubico.YubicoUtils;
import com.yubico.webauthn.data.ByteArray;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class YubicoUtilsTest {

  @Test
  public void testByteArrayToUUIDConversion() {
    UUID uuid1 = UUID.randomUUID();
    ByteArray byteArray = YubicoUtils.toByteArray(uuid1);

    UUID uuid2 = YubicoUtils.toUUID(byteArray);

    Assertions.assertThat(uuid1).isEqualTo(uuid2);
  }
}
