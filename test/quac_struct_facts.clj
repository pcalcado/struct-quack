(use 'fato)

(sheet "a duck typed struct"
       (fact "is based on a struct defined using 'defstruct")
       (fact "has its defining struct as metadata")
       (fact "throws exception when trying to set inexistent slot")
       (fact "throws exception when trying to get inexistent slot"))