import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.extra.camera.Orbital
import org.openrndr.extra.camera.ProjectionType
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.meshgenerators.boxMesh
import org.openrndr.extra.meshgenerators.buildTriangleMesh
import org.openrndr.extra.meshgenerators.extrudeShape
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.XYParameter
import org.openrndr.extra.viewbox.viewBox
import org.openrndr.math.Spherical
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.shape.Circle

fun main() = application {
    configure {
        width = 1280
        height = 720
    }
    oliveProgram {

        val gui = GUI()

        val settings = object {
            @XYParameter("xy", 0.0, 1.0, 0.0, 1.0)
            var xy = Vector2.ZERO
        }.addTo(gui)

        val texture = loadImage("data/images/moon_8k.jpg")

        val discs = (6 downTo 1).map {
            val w = 50.0 + (40.0 * it) + Double.uniform(-20.0, 20.0)
            val c = Circle(Vector2.ZERO, w)
            val z = (255.0 + Double.uniform(-25.0, 20.0)) * it
            c to z
        }

        extend(gui)

        val vb = viewBox(drawer.bounds) {

            val o = extend(Orbital()) {
                eye = Vector3.UNIT_Z * -300.0
                far = 2000.0
                near = 0.1
                userInteraction = true
            }

            hasInputFocus = false

            extend {
                o.projectionType = ProjectionType.ORTHOGONAL

                discs.forEachIndexed { i, (c, z) ->
                    drawer.isolated {
                        drawer.depthWrite = true
                        drawer.depthTestPass = DepthTestPass.ALWAYS
                        drawer.shadeStyle = shadeStyle {
                            fragmentTransform = """
                            
                            vec2 texCoord = c_boundsPosition.xy + p_pos;
                            texCoord.y = 1.0 - texCoord.y;
                            vec2 size = textureSize(p_tex, 0);
                            texCoord.x /= size.x/size.y;
                            
                            x_fill = texture(p_tex, texCoord);
                        
                            """.trimIndent()
                            parameter("tex", texture)
                            parameter("i", i)
                            parameter("pos", settings.xy)

                        }
                        drawer.translate(0.0, 0.0, z)
                        drawer.shape(c.shape)
                    }
                }

            }
        }



        extend {

            //o.camera.setView(Vector3.UNIT_Z * -300.0, Spherical(-215.0, 75.0, 80.0), 50.0)
            vb.draw()
        }
    }
}