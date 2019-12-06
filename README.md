# Solveur PPC
Le but est d'implémenter un solveur de programmation par contraintes qui soit fonctionnel. Est-il optimisé ? Non. Mais il est facile à utiliser.  
La première partie va porter sur comment utiliser le solveur, et la deuxième partie sur l'implémentation de celui-ci. 
Je vais parler très brièvement des choix que j'ai fait et pourquoi, ainsi que des difficultés rencontrées 
(pour plus de détails, voir **directement dans le code**. Il est commenté, un peu.) 

## I. Modélisation d'un problème
### 1. Créer un modèle
Pour créer un modèle, on passer par la classe Model. Elle permet de centraliser toutes les informations concernants les 
variables, les contraintes, etc... et permet de facilement créer les différents élements.  
`Model model = new Model();`

### 2. Ajouter des variables au modèle
Il existe plusieurs manières d'ajouter des variables au modèle. Tout d'abord, on peut les ajouter une par une. 
Soit on défini le tableau des valeurs possibles, soit on défini la borne inférieur et la borne supérieur du domaine.
  
 _Création d'une variable dont le domaine est {1, 2, 3}_  
`Variable variable = model.addVariable(new int[]{1, 2, 3});`  
`Variable variable = model.addVariable(1, 3);`

Ces deux syntaxes sont équivalentes. Il est préférable d'utiliser la deuxième dans le cas d'un domaine continu, tandis 
qu'il sera seulement possible d'utiliser la première dans le cas d'un domaine discontinu.  
On peut également décider de créer un tableau de variables. Il s'agit de la même construction que pour la création d'une 
seule variable, en rajoutant évidemment combien de variables il faut créer.

_Création de n variables dont le domaine est {1, 2, 3}_  
`Variable[] variables = model.addVariables(n, new int[]{1, 2, 3});`  
`Variable[] variables = model.addVariables(n, 1, 3);`

### 3. Définir les contraintes
Pour définir une contrainte, il suffit d'exprimer cette contrainte sous la forme d'une équation mathématique, 
et d'appeler la fonction `model.addConstraint()` comme suit :

_Contrainte simple où variable[0] < variable[1]_  
`model.addConstraint("x < y", variable[0], variable[1]);`

L'association symbole -> variable se fait automatiquement. Pour plus de précision, voir la partie sur la classe Expression.  
Quelques exemples un peu plus foufous :

`model.addConstraint("(| x - y | + 5) % 5", var[0], var[1]);`  
`model.addConstraint("x^2 + y + z * (5 * (a + b))", v[0], v[1], v[2], v[3], v[4]);`  
`model.addConstraint("x = 0", variables[0]);`

### 4. Définir les variables de décisions
Pour avoir plus de contrôle sur le modèle, on défini manuellement quelles sont les variables pour lesquelles on doit 
effectuer un choix. Cela permet de rajouter plusieurs variables dépendantes d'autres variables qui vont servir dans les 
contraintes sans augmenter le parcours de l'arbre. Je n'ai pas eu le temps de faire un système optimisé automatique.  

`model.decisionVariables(variables); // ajoute un tableau de variables`  
`model.decisionVariable(variable); // ajoute une variable`  

### 5. Définir une fonction objectif
Pour l'instant, la fonction objectif n'est pas encore pleinement implémentée. Il est possible de poser des objectifs 
simples, mais pas encore des objectifs aussi complexes que l'on veut.  
Il est possible de définir une fonction objectif à l'aide d'une expression. En plus des expressions classiques, 
il est possible de définir une somme en fonction d'un pattern. La fonction objectif s'effectue sur des variables que 
l'on passe en paramètre (donc pas forcément sur toutes les variables du domaines).  
On peut définir plusieurs objectifs, ceux-ci seront étudiés par ordre de priorité FIFO.

`model.minimize("val", variable[n-1]); // minimise la dernière variable`  
`model.maximize("v1 + v2", variable[0], variable[1]); // maximise la somme des deux premières variables`  
`model.minimize(ExpressionBuilder.sum("var", N), variables); // minimise la somme du tableau de variables (de taille N)`  

Pour plus d'information sur ExpressionBuilder, voir la section sur les Expressions.  

### 6. Paramètres de résolution et métriques
#### Filtrage
Définir le type de filtrage : `model.setFilter(Constraint.AC3)` ou `model.setFilter("AC3")`.  
Les types de filtrage disponibles sont AC3, AC4, AC6 et AC2001.  

#### Recherche de solutions
On peut définir le nombre de solutions que l'on désire avoir.  
`model.lookingForSolution(n)` pour recherche n solutions. Si n <= 0, alors on recherche toutes les solutions.  

_Attention : Malgré la présence d'une fonction objectif, rechercher n solutions retournera les n premières solutions trouvées, 
et pas les n solutions optimales._  

#### Lancer une résolution et afficher les "statistiques" du solveur
Lancer la résolution : `model.solve();`  
Récupérer les statistiques : `String stats = model.stats();`  
Récupérer les solutions : `int[][] solutions = model.solutions();`  
Récupérer la meilleure solution (fonction objectif) : `int[] best = model.best();`

Exemple d'affichage _(GolombRuler n = 7, ub = 30, objectif : minimiser la dernière valeur)_ :   
`Building (s) : 0.242`  
`Execution (s) : 2.038`  
`# Fails : 1291`  
`# Backtracks : 1186`  
`# Solutions : 429 (all solutions)`  
`CONSTRAINT : AC2001`  
`OPTIMAL SOLUTION : 0 2 7 13 21 22 25` 

## Expression
### Définition
Les Expressions permettent de passer d'une String à un arbre binaire évaluable. 
Toutes les priorités d'opérations sont implémentées, et il est possible d'ajouter des expressions parenthésées.
Exemples :  

| String | Expression |
|--------|------------|
|`"x + 2 * y < 6 * x"` | `((x + (2 * y)) < (6 * x))` |
|`"(x + 2) * y < 6 * x"` | `(((x + 2) * y) < (6 * x))` |
|`"x^2 * 3 != 7"` | `(((x ^ 2) * 3) != 7)` |

Certaines opérations sont automatiquement simplifiées. Cependant, je n'ai pas pu tester tous les cas, donc aucune 
garantie que cela fonctionne correctement. Pour les cas d'utilisation "simple", je n'ai trouvé aucun bug.

| String | Expression |
|--------|------------|
|`"x^(2 * 3) != 7"` | `((x ^ 6) != 7)` |
|`"1 - 1 + 1 - 1 + 1 - 1 + 1 - 1 < x"` | `(0 < x)` |
|`"x^0 = y"` | `(1 = y)` |
|`"0 * 10^10^10 = y"` | `(0 = y)` |

### Évaluation
Pour évaluer une expression, il faut passer en paramètre un tableau d'entiers de taille égal au nombre de variables dans 
l'expression. Les valeurs sont automatiquement associées aux variables. Par exemple, `x + y < 5` attends un tableau de 
taille 2, `x + y < z` un tableau de taille 3 et `x + x + x < 5` un tableau de taille 1.  
Si l'expression est une comparaison, alors l'évaluation renvoit `true` si la comparaison est vraie ou `false` si elle est fausse.

`Expression lt = ExpressionBuilder.create("x < y");`  
`lt.eval(1, 2); => true`  
`lt.eval(1, 1); => false`  
`Expression triple_lt_10 = ExpressionBuilder.create("x + x + x < 10");`  
`triple_lt_10.eval(3); => true`  
`triple_lt_10.eval(4); => false`  

Si l'expression est une opération, alors l'évaluation renvoit un entier.

`Expression x_plus_y = ExpressionBuilder.create_arith("x + y");`  
`x_plus_y.eval_int(1, 1); => 2`  
`x_plus_y.eval_int(4, 5); => 9`  

**Attention :**  
Appeler `eval_int` ou `create_arith` sur une expression qui est une comparaison provoque une erreur.  
De même, `eval` ou `create` sur une Expression qui n'est pas une comparaison provoque une erreur.

### ExpressionBuider
ExpressionBuilder permet de construire des expressions sans se soucier de l'implémentation.
Cette classe dispose de 4 fonctions seulement.  
- `create` permettant de créer une comparaison.
- `create_arith` permettant de créer une expression arithmétique.
- `sequence` permettant de créer une suite arithmétique
- `sum` cas particulier de `sequence` où l'opérateur est `+`.

#### Sequence
Pour créer une séquence, on passe 3 paramètres en entrée : un motif qui sera répété, un opérateur et le nombre d'élément 
dans la suite. Par exemple, pour créer la suite `x[1] + x[2] + x[3]`, on écrit `sequence("var", "+", 3);` ou `sum("var", 3);`.
L'expression générée est `(var_0 + (var_1 + var_2))`.  
Il existe 2 tokens réservés pour parse les suites. Comme on vient de le voir, le premier token est `var`, et il représente une variable qui change 
au fur et à mesure de la suite. Le deuxième est `i`, et il représente le compteur (allant de 0 à n-1). Ainsi, `sum("i", 3);` représente la
suite `0 + 1 + 2`, et `sum("i^2", 3);` représente `0^2 + 1^2 + 2^2`.  
Il est possible d'utiliser plusieurs fois ces tokens dans le motif, mais il sera toujours associé à la même valeur.  
`sum("i*i", 3); -> 0*0 + 1*1 + 2*2 -> 5`.  
`sum("var + var", 3); -> ((var_0 + var_0) + ((var_1 + var_1) + (var_2 + var_2)))`.  
`sum("var*i + i", 3); -> ((var_1 + 1) + ((var_2 * 2) + 2))`.  

On peut également ajouter d'autres variables ou constantes (qui resteront les mêmes).  
`sum("var*i + x", 3); -> (x + ((var_1 + x) + ((var_2 * 2) + x)))`  
`sum("var^2 + x", 3); -> (((var_0 ^ 2) + x) + (((var_1 ^ 2) + x) + ((var_2 ^ 2) + x)))`  


Comme pour les Expressions, il faudra s'assurer de passer le bon nombre de valeurs lors de l'évaluation 
(et dans le bon ordre !). 

## II. Implémentation

