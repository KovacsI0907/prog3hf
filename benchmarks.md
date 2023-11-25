### Bilateral filter execution times
#### 515 MB 152 items

| kernel size | 1 Thread | 10 threads |
|-------------|----------|------------|
| 3           | 49s      | 49s        |
| 9           | 67s      | 47s        |
| (11)        | (98s)    | -          |
| 15          | 179s     | 63s        |
| 21          | 351s     | 96s        |
| 27          | 582s     | 154s       |

c1..ci are constants
Size of input : n pixels
bilateral filter n pixels
for kernel size k
1 pixel bilateral filter -> c1 * k * k steps
for n pixels -> n * (c1 * k * k + c2) = c1 * (n * k * k) + c3
--> time complexity: O(n*k^2)

sobel operator: O(n) (k * k is always 9)
median filtering: O(n * k^2)


MEMORY

8 bytes per pixel
1 MB chunks = 1024KB = 1048576 B = ~130K pixels
(width + paddingSize * 2) * (tileHeight + paddingSize * 2) = 130K
w(t+2p) + 2p(t+2p) = wt + 2wp + 2pt + 4p^2 ->
-> wt + 2pt = 130K - 2wp - 4p^2
-> t = (130K - 2wp - 4p^2) / (w + 2p)
tileHeight = 130K/width but at least padding size
