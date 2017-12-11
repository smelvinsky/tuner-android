package com.smelvinsky.tuner;

/**
 * Created by smelvinsky on 10.12.17.
 */

enum Octave
{
    SubContra(0), Contra(1), Great(2), Small(3), oneLine(4), twoLine(5), threeLine(6), forLine(7), fiveLine(8);

    private int octaveNumber;

    Octave(int octaveNumber)
    {
        this.octaveNumber = octaveNumber;
    }

    public int getOctaveNumber()
    {
        return octaveNumber;
    }

    public static Octave getOctaveFromNumber(int octaveNumber)
    {
        return Octave.values()[octaveNumber];
    }
}

enum Note
{
    //European Notation (US B = EU H) !!!
    C(0), Cis(1), D(2), Dis(3), E(4), F(5),
    Fis(6), G(7), Gis(8), A(9), Ais(10), H(11);

    private int halfStepsNumber;

    Note(int halfStepsNumber)
    {
        this.halfStepsNumber = halfStepsNumber;
    }

    public int getHalfStepsNumber()
    {
        return halfStepsNumber;
    }

    public static Note getNoteFromHalfStepsNumber(int halfStepsNumber)
    {
        return Note.values()[halfStepsNumber];
    }
}


class NoteObject
{
    private Note note;
    private Octave octave;

    NoteObject(Note note, Octave octave)
    {
        this.note = note;
        this.octave = octave;
    }

    Note getNote()
    {
        return note;
    }

    Octave getOctave()
    {
        return octave;
    }
}
