# miniCP
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

#### Opérateurs disponibles
Il est possible de changer les symboles dans la classe Expression.
- Comparaison : `<`, `<=`, `>`, `>=`, `=`, `!=`
- Opération : `+`, `-`, `*`, `/`, `%`, `^`
- Spécial : `(`, `)`, `|` (valeur absolue)


### ExpressionBuider
ExpressionBuilder permet de construire des expressions sans se soucier de l'implémentation.
Cette classe dispose de 4 fonctions seulement.  
- `create` permettant de créer une comparaison.
- `create_arith` permettant de créer une expression arithmétique.
- `sequence` permettant de créer une suite arithmétique
- `sum` cas particulier de `sequence` où l'opérateur est `+`.

#### Suites
Pour créer une suite, on passe 3 paramètres en entrée : un motif qui sera répété, un opérateur et le nombre d'élément 
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

### 1. Le domaine
Le domaine est implémenté sous la forme d'un **ensemble réversible non-ordonné**. A l'échelle de ce solveur, conserver
l'ordre des valeurs me semble peu important. Le seul moment où il semble intéressant de conserver l'ordre est lors
du choix de l'affectation d'une valeur à une variable. En effet, conserver l'ordre permettrait de savoir quelle valeur
n'a pas déjà été affecté à l'aide d'un simple entier (pointeur), qui pointerait l'index de la dernière valeur affectée.
Les seules opérations seraient l'incrémentation et la remise à zero. En choisissant de ne pas conserver l'ordre, il faut
alors conserver la liste des valeurs qui ont déjà été affectées (O(n) en mémoire, n taille du domaine) et tester lors
de chaque nouvelle affectation si la valeur n'a pas déjà été affecté (O(n²) en temps, n taille du domaine). On gagne de
la complexité lors de la restauration du domaine, mais on en perd lors de l'affectation d'une variable. A priori, on fait
souvent plus de restaurations que d'affectations, ce qui a motivé mon choix. L'idéal serait d'implémenter les deux et de
tester lequel est plus rapide.

### 2. Le delta
Le delta est un simple tableau d'éléments. On dispose d'un pointeur pour connaître la fin du tableau, et c'est tout.
Les seules opérations réelles sont l'incrémentation et la décrémentation du pointeur, et l'affectation d'un élement du 
tableau à un nouvel entier. Pour ce solveur, pas besoin de plus.

### 3. Contraintes, tables et expressions
Dans ce solveur, toute contrainte est **binaire**. Cependant, il peut arriver que certaines contraintes doivent lier plus
de deux variables : dans ce cas, on créer une nouvelle "fausse" variable permettant de faire le lien entre les autres
variables.

| x | y | z | N |
|---|---|---|---|
| 0 | 1 | 1 | 0 |
| 0 | 1 | 2 | 1 |
| 1 | 2 | 1 | 2 |
|...|...|...|...|
| 8 | 8 | 8 | n |

On créer ensuite les contraintes binaires (x, N), (y, N) et (z, N).

#### Construction des tables et évaluation retardée
Comme je l'ai montré plus haut, l'ajout de contraintes se fait par un objet de type Expression. En réalité, toutes les
expressions sont stockées et leur évaluation est retardée jusqu'au lancement du solveur.  
Les expressions sont stockées en fonction de leur arité : les expressions unaires sont dans une map <Variable, List<Expressions>>,
les expressions binaires sont dans un set de Couple (en réalité : deux variables + une liste d'expressions) et les
expressions n-aires sont dans un set de NCouple (en réalité : n-variables + une liste d'expressions).  

Pourquoi faire ça ? Tout simplement pour simplifier les intersections de contraintes. Si je pose une contrainte entre deux 
variables, et une deuxième entre ces deux variables, alors je veux en réalité qu'une seule table, c'est-à-dire une
seule contrainte (qui est l'intersection de ces deux contraintes). En retardant l'évaluation et la création des tables,
je m'assure qu'on dispose de toutes les contraintes et que tous les couples générés répondent à toutes les contraintes
(ce qui représente un gain en mémoire ET en temps).

Ainsi, écrire les contraintes `x > y` et `y < x` ne génère qu'une seule table, comme on est en mesure de l'attendre.
Dans un cas général, poser les contraintes C1, C2 et C3 entre x et y revient à poser la contrainte C1 /\ C2 /\ C3.
De plus, poser une contrainte entre x et y est équivalent à poser une contrainte entre y et x.

#### Tables
Les tables sont implémentées sous forme d'une table de hachage, ce qui permet d'accéder plus rapidement aux tuples valides.
Afin de trouver un compromis espace/temps, la table de hash est uniquement dans le sens x -> y. Pour trouver les tuples (x, ...) valides,
c'est en O(1). Pour trouver les tuples (y, ...) valides, c'est en O(n).  
Petit gain possible lors de l'AC6 en mémorisant uniquement la liste des supports de y, étant donné que l'accès aux tuples
valides de x est plus rapide.

### 4. Supports
#### AC4 et AC6
Dans le cas de l'AC4 et l'AC6, la liste des supports est implémentée à l'aide d'un ensemble réversible non-ordonné.
Contrairement au domaine où un ensemble ordonné peut avoir son intérêt (puisqu'on veut savoir quelles valeurs ont déjà
été étudiées), ici on ne s'intéresse qu'au cas où une valeur n'a plus de support, c'est-à-dire qu'on s'intéresse seulement
à la taille de la liste de support. Le fait que la liste des supports soit ordonnée ou pas ne fait pas gagner de temps,
mais le fait qu'elle ne soit pas ordonnée est un gain de temps lors de la restauration de l'état.

#### AC2001
Dans le cas de l'AC2001, ce n'est plus une liste de supports mais simplement un seul support par valeur. Pour gagner du
temps, lorsqu'une valeur n'a plus de support, je garde tout de même en mémoire l'ancien support tout en supprimant
la valeur du domaine. Ainsi, plus besoin de restaurer l'état, puisque si un support est valide à l'étape i+1, alors il 
est valide à l'étape i. Comme j'effectue l'itération sur les domaines, le fait de garder en mémoire un support n'est pas
grave puisque la valeur n'existe plus dans le domaine (et ne sera donc pas prise en compte lors de la propagation).
Cela permet de gratter un peu de complexité en temps et en espace (puisque je n'ai pas à sauvegarder les états précédents).

## III. Benchmarks, tests et explications
### 1. N-Queens
_Tests  réalisés pour N = 12._  

| Filter | Building time (ms) | Execution time (s) | Ranking |
|--------|--------------------|--------------------|---------|
| AC3    | 18                 | 2.23               |    2    |
| AC4    | 43                 | 2.42               |    4    |
| AC6    | 26                 | 2.307              |    3    |
| AC2001 | 18                 | 2.132              |    1    |

_Tests  réalisés pour N = 14._   

| Filter | Building time (ms) | Execution time (s) | Ranking |
|--------|--------------------|--------------------|---------|
| AC3    | 40                 | 62.229             |    2    |
| AC4    | 48                 | 65.937             |    3    |
| AC6    | 42                 | 65.971             |    4    |
| AC2001 | 40                 | 53.498             |    1    |

#### Pourquoi AC4 et AC6 sont-ils plus lent qu'AC3 ?
Il y a à priori deux explications : soit j'ai raté l'implémentation d'AC4 et d'AC6, soit le modèle ne se prête pas bien
à ces deux filtrages (ou : les deux en même temps). En effet, si l'on passe plus de temps à faire de la restauration
qu'à faire du filtrage, alors AC4 et AC6 ne sont pas très adaptés au modèle. Pour savoir s'il s'agit réellement d'un 
problème de modèle, nous allons effectuer des tests sur un autre modèle.

### 2. Golomb Ruler

_Tests  réalisés pour N = 7, UB = 30._  

| Filter | Building time (ms) | Execution time (s) | Ranking |
|--------|--------------------|--------------------|---------|
| AC3    | 324                | 3.315              |    4    |
| AC4    | 397                | 2.707              |    2    |
| AC6    | 383                | 3.204              |    3    |
| AC2001 | 327                | 2.383              |    1    |

_Tests  réalisés pour N = 8, UB = 40._  

| Filter | Building time (ms) | Execution time (s) | Ranking |
|--------|--------------------|--------------------|---------|
| AC3    | 443                | 29.045             |    4    |
| AC4    | 482                | 28.591             |    2    |
| AC6    | 504                | 28.72              |    3    |
| AC2001 | 455                | 27.81              |    1    |

Comme on le voit, AC3 est le pire filtrage possible pour résoudre le Golomb Ruler ; comme je le soupçonnais, il y a bien
une histoire de modèle adapté à certains filtrages.

### 3. Analyse des résultats
Clairement, AC2001 semble être un bon compromis temps/mémoire. Il se classe n°1 dans les deux modèles, pour les deux
configurations testées. Étant donné qu'il s'agit d'un AC3 avec une petite mémoire, il est d'ailleurs normal (ou du moins
attendu) qu'il soit meilleur.  
Concernant le cas de l'AC4 et de l'AC6, on voit qu'ils prennent beaucoup de temps lors de la restauration des supports
(comme le montre les résultats du N-Queens). Malgré ceci, on voit que pour des problèmes plus complexes, la complexité - 
en négligeant la restauration - est meilleure (comme le montre les résultats du GolombRuler).