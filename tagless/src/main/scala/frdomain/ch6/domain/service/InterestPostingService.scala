package frdomain.ch6
package domain
package service

trait InterestPostingService[M[_], Account, Amount] 
  extends InterestCalculation[M, Account, Amount]
  with TaxCalculation[M, Amount]

