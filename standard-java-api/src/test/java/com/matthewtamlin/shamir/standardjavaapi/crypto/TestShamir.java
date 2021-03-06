/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.shamir.standardjavaapi.crypto;

import com.google.common.collect.ImmutableSet;
import com.matthewtamlin.shamir.commonslibrary.model.CreationScheme;
import com.matthewtamlin.shamir.commonslibrary.model.RecoveryScheme;
import com.matthewtamlin.shamir.commonslibrary.model.Share;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import static java.math.BigInteger.ONE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for the {@link Shamir} class.
 */
@SuppressWarnings("ConstantConditions")
public class TestShamir {
  private static final BigInteger FIVE = BigInteger.valueOf(5);
  
  private static final BigInteger SEVEN = BigInteger.valueOf(7);
  
  private Shamir shamir;
  
  @Before
  public void setup() {
    shamir = new Shamir(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiate_nullRandom() {
    new Shamir(null);
  }
  
  @Test
  public void testInstantiate_nonNullRandom() {
    new Shamir(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInstantiateStatically_nullRandom() {
    Shamir.create(null);
  }
  
  @Test
  public void testInstantiateStatically_nonNullRandom() {
    Shamir.create(new SecureRandom());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateShares_nullSecret() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(2)
        .setPrime(7)
        .build();
    
    shamir.createShares(null, creationScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testCreateShares_nullCreationScheme() {
    shamir.createShares(ONE, null);
  }
  
  @Test
  public void testCreateShares_secretLessThanPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(7)
        .build();
    
    shamir.createShares(FIVE, creationScheme);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateShares_secretEqualToPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(5)
        .build();
    
    shamir.createShares(FIVE, creationScheme);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateShares_secretGreaterThanPrime() {
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(2)
        .setTotalShareCount(3)
        .setPrime(5)
        .build();
    
    shamir.createShares(SEVEN, creationScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_nullShares() {
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    shamir.recoverSecret(null, recoveryScheme);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_nullRecoveryScheme() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(1).setValue(2).build())
        .build();
    
    shamir.recoverSecret(shares, null);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testRecoverSecret_sharesContainsNull() {
    final Set<Share> shares = new HashSet<>();
    
    shares.add(Share.builder().setIndex(1).setValue(1).build());
    shares.add(null);
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    shamir.recoverSecret(shares, recoveryScheme);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testRecoverSecret_duplicateShareIndex() {
    final Set<Share> shares = ImmutableSet
        .<Share>builder()
        .add(Share.builder().setIndex(1).setValue(1).build())
        .add(Share.builder().setIndex(1).setValue(2).build())
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(2)
        .setPrime(7)
        .build();
    
    shamir.recoverSecret(shares, recoveryScheme);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_noSharesRecovered() {
    createSharesAndRecoverSecret(2, 2, 0);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_oneShareRecovered() {
    createSharesAndRecoverSecret(2, 2, 1);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_twoTotalParts_twoSharesRecovered() {
    createSharesAndRecoverSecret(2, 2, 2);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_noShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 1);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_oneShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 1);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_twoShareRecovered() {
    createSharesAndRecoverSecret(2, 3, 2);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_twoRequiredParts_threeTotalParts_threeSharesRecovered() {
    createSharesAndRecoverSecret(2, 3, 3);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_nineSharesRecovered() {
    createSharesAndRecoverSecret(10, 100, 9);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_tenShareRecovered() {
    createSharesAndRecoverSecret(10, 100, 10);
  }
  
  @Test
  public void testCreateSharesAndRecoverSecret_tenRequiredParts_oneHundredTotalParts_elevenSharesRecovered() {
    createSharesAndRecoverSecret(10, 100, 11);
  }
  
  private void createSharesAndRecoverSecret(
      final int requiredShareCount,
      final int totalShareCount,
      final int recoveredShareCount) {
    
    final BigInteger secret = new BigInteger("1298074214633706835075030044377087");
    final BigInteger prime = new BigInteger("1298074214633706835075030044421213");
    
    final CreationScheme creationScheme = CreationScheme
        .builder()
        .setRequiredShareCount(requiredShareCount)
        .setTotalShareCount(totalShareCount)
        .setPrime(prime)
        .build();
    
    final RecoveryScheme recoveryScheme = RecoveryScheme
        .builder()
        .setRequiredShareCount(requiredShareCount)
        .setPrime(prime)
        .build();
    
    final Shamir shamirProvider = new Shamir(new SecureRandom());
    
    final Set<Share> allShares = shamirProvider.createShares(secret, creationScheme);
    
    final Set<Share> recoveredShares = Observable
        .fromIterable(allShares)
        .take(recoveredShareCount)
        .collectInto(new HashSet<Share>(), Set::add)
        .blockingGet();
    
    final BigInteger reconstructedSecret = shamirProvider.recoverSecret(recoveredShares, recoveryScheme);
    
    assertThat("Recovered secret does not match original secret.", reconstructedSecret, is(secret));
  }
}