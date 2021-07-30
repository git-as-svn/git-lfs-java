package ru.bozaro.gitlfs.common.data

/**
 * Object locations.
 *
 * @author Artem V. Navrotskiy
 */
interface Links {
    val links: Map<LinkType, Link>
}
