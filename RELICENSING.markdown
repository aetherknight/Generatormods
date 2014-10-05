# Relicensing Generator Mods

I, William (B.J.) Orvis, believe that it is not possible for a Minecraft mod
licensed under the GPL to comply with the GPL. Minecraft mods are essentially
plugins for a closed-source program, and according to the Free Software
Foundation (the organization that authored the GPL), [it is not possible for
such a compiled plugin to comply with the
GPL](http://www.gnu.org/licenses/gpl-faq.html#GPLPluginsInNF):

> If the program dynamically links plug-ins, and they make function calls to
> each other and share data structures, we believe they form a single program,
> which must be treated as an extension of both the main program and the
> plug-ins. This means that combination of the GPL-covered plug-in with the
> non-free main program would violate the GPL. However, you can resolve that
> legal problem by adding an exception to your plug-in's license, giving
> permission to link it with the non-free main program.

While it is possible to add an exception to the license in order to allow a mod
to link against Minecraft, Forge, and other non-"free" mods, such an exception
would have to be carefully worded to allow it to be used in other, unconsidered
use-cases (Eg, you might not be able to re-use the code from the mod with a
different closed-source game, or with some other combination of mods or modding
frameworks). On the other hand, the LGPL allows a library to be linked to
anything else, but it still protects the source code of the mod.

In order to relicense the mod (be it under the GPL with an exception or the
LGPL), all authors would have to agree to relicense their own contributions
under the new license. I have done this by asking both prior authors, Noah
Whitman (Formivore) and Olivier Sylvain (GotoLink), and getting permission from
them to relicense the source code under the LGPL.

## Permission from all the previous authors to relicense under the LGPL

From Noah's reply email:

> Thanks for informing me about the problem in linking a GPL project with
> Minecraft. I've now re-licensed to LGPL all of Generator mods code in the
> google code repository. I was the sole author of all of the Generator mods
> source.

Noah also updated the original SVN repository to relicense the files.

From Olivier's reply email:

> Anyways, in case such licensing isn't to your taste, as a secondary author, i
> hereby:
>
> * agree to re-license "WalledCity, Great Wall, and Ruins" mods under LGPL v3
> * grant you the exemption to statically or dynamically link to any part of
>   "WalledCity, Great Wall, and Ruins" mods and keep the code for it under LGPL,
>   or any other compatible license
>
> Note that all other authors (Formivore) have to agree for above statement to apply.

## Tracking the relicensing

I have already rebased Olivier's git commits from onto the point in Noah's SVN
repository where Olivier forked it in order to gain a complete version control
history of the source code. More recently, Noah added new SVN commits to update
the licensing after my request. I have also merged in those new commits as part
of my effort to update the documentation of the licensing.
