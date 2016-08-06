/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.dscid.filesystemanalyzer;

/**
 *
 * @author felix
 */
public class PrimeRepresentation {
  long prime;
  long value;
  public PrimeRepresentation(long n){
    if (n==0){
      //throw new IllegalArgumentException("Zero not supported");
    }
    value = n;
    prime = n;
    while (!isPrime(prime)) {
      prime --;
    }
  }
  public Long getPrime(){
    return prime;
  }
  public Long getValue(){
    return value;
  }
  
  @Override
  public String toString(){
    return String.format("%d + %d", prime, (value - prime));
  }
  static boolean isPrime(long n) {
    //check if n is a multiple of 2
    if (n % 2 == 0) {
      return false;
    }
    //if not, then just check the odds
    for (int i = 3; i * i <= n; i += 2) {
      if (n % i == 0) {
        return false;
      }
    }
    return true;
  }
}
