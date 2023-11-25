- [x] extend PngInfo to calculate bpp for all image types
- [x] test2.png display fix
- [x] implement paeth filtering
- [x] better system for filtering functions
- [ ] other color type and bit depth support
    - [x] variable bitPerPixel in filtering
    - [ ] 16 bit support
- [ ] clean up PngInput.java
- [ ] Rewrite main function
- [x] Implement padding for image tiles
- [ ] Change image tile to store in int
- [ ] Clamp down 16 bit color OR implement support
- [x] Lower tile and padding fix
- [ ] Better exception handling in run() functions
- [x] Grey line bug
- [ ] refactor IDATLOADER line ~46
- [ ] include outputwriter tiles in memory quota
- [ ] rewrite getting tiles with padding
- [ ] fix +1 padding needed

|                           | Colour type | Allowed bit depths     |
|---------------------------|-------------|------------------------|
| **Greyscale**             | 0           | ~~1, 2, 4,~~ ~~8~~, 16 |
| **Truecolour**            | 2           | ~~8~~, 16              |
| ~~**Indexed-colour**~~    | ~~3~~       | ~~1, 2, 4, 8~~         |
| **Greyscale with alpha**  | 4           | ~~8~~, 16              |
| **Truecolour with alpha** | 6           | ~~8~~, 16              |
