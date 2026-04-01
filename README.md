# pslg-point-location-chains
# Point Location in PSLG using Monotone Chains

## 1. Requirement

The objective is to determine the position of a query point **M** within a planar subdivision induced by a **Planar Straight Line Graph (PSLG)**. The graph is represented using a **Doubly Connected Edge List (DCEL)** structure.

The goal is to identify the specific **face** of the subdivision that contains point **M**.

### Data Structure (DCEL)
Each edge in the graph is defined by:
* **V1, V2:** Start and end vertex coordinates.
* **F1, F2:** Indices of the Left and Right faces (Face 0 is the external/unbounded face).
* **P1, P2:** Indices of the next edges in counter-clockwise order around V1 and V2.

---

## 2. Methodology: The Chain Method

The **Chain Method** solves the point location problem by decomposing the PSLG into a complete set of **y-monotone chains** that are ordered from left to right and do not intersect.

### Phase I: Pre-processing
1. **Vertex Sorting:** Vertices are sorted by their Y-coordinates ($y_1 < y_2 < \dots < y_n$).
2. **Edge Orientation:** Edges are oriented from the vertex with the lower Y-coordinate to the one with the higher Y-coordinate.
3. **Weight Balancing:** * Each edge is assigned a weight representing the number of chains passing through it.
   * We ensure that for every intermediate vertex $v_i$, the sum of weights of incoming edges equals the sum of weights of outgoing edges: $W_{in}(v_i) = W_{out}(v_i)$.
   * This is achieved through two passes (bottom-up and top-down).

### Phase II: Point Localization
The location of point **M(x_M, y_M)** is performed in two logarithmic steps:
1. **Chain Selection (Binary Search):** Since chains are ordered left-to-right, we use binary search to find the two adjacent chains that horizontally bracket point **M**.
2. **Edge Localization:** Within the identified chains, we locate the specific edge covering the Y-range of $y_M$ and use a determinant-based orientation test to confirm the relative position.

---

## 3. Practical Example

### PSLG Edge Data (Partial)
| Edge | V1 (x, y) | V2 (x, y) | F1 (L) | F2 (R) | P1 | P2 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | (-15, 5) | (-3, 8) | 0 | 1 | 3 | 13 |
| 2 | (1, 7) | (8, 10) | 2 | 5 | 16 | 15 |
| ... | ... | ... | ... | ... | ... | ... |

### Vertex Adjacency (In/Out)
The pre-processing phase identifies incoming ($A_i$) and outgoing ($B_i$) edges for each vertex to prepare for weight balancing:
* **Vertex 1:** $A(i) = \emptyset$, $B(i) = \{11, 13, 7, 9\}$
* **Vertex 2:** $A(i) = \{11\}$, $B(i) = \{3, 4\}$
* *(and so on for all 12 vertices)*

### Result
By building the monotone chains and performing the double binary search, the algorithm efficiently identifies the **Face ID** where point **M** resides with $O(\log^2 n)$ or $O(\log n)$ query time depending on the implementation.
